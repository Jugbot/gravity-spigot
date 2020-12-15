package io.github.jugbot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.collect.Table.Cell;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.block.Block;

import io.github.jugbot.graph.GraphState;
import io.github.jugbot.graph.SubGraph;
import io.github.jugbot.graph.SuperGraph;
import io.github.jugbot.util.AsyncBukkit;
import io.github.jugbot.util.IntegerXZ;

/**
 * Takes block changes and outputs offending blocks that should fall asynchronously. Note reported blocks may be in
 * other chunks.
 */
public class ChunkProcessor {
  private static ChunkProcessor instance;

  public static ChunkProcessor Instance() {
    if (instance == null) instance = new ChunkProcessor();
    return instance;
  }

  /** Fires chunk processing every tick */
  private ChunkProcessor() {
    // LoadingCache<Chunk, SubGraph> graphs = CacheBuilder.newBuilder()
    //    .maximumSize(10)
    //    .expireAfterWrite(30, TimeUnit.SECONDS)
    //    .removalListener(SubGraphIO.Instance())
    //    .build(SubGraphIO.Instance());

    Bukkit.getScheduler()
        .scheduleSyncRepeatingTask(
            App.Instance(),
            new Runnable() {
              @Override
              public void run() {
                ChunkProcessor.Instance().processChunks();
              }
            },
            0,
            1);
  }

  /**
   * Load SubGraph alongside Chunk
   *
   * @see ChunkListener
   */
  public void loadChunk(Chunk chunk) {
    SubGraph integrityChunk = loadedChunks.get(chunk);
    // if (integrityChunk == null) {
    //   integrityChunk = SubGraphIO.Instance().loadChunk(chunk.getWorld().getName(), chunk.getX(), chunk.getZ());
    // }
    if (integrityChunk == null) {
      App.Instance().getLogger().fine("Creating Chunk " + chunk.toString());
      integrityChunk = new SubGraph(chunk);
      App.Instance().getLogger().fine("Created Chunk " + chunk.toString());

      // SubGraphIO.Instance().saveChunk(integrityChunk);
    }
    loadedChunks.put(chunk, integrityChunk);
  }

  /**
   * Save SubGraph alongside Chunk
   *
   * @see ChunkListener
   */
  public void unloadChunk(Chunk chunk) {
    // SubGraph integrityChunk = loadedChunks.remove(chunk);
    // if (integrityChunk != null) {
    //   SubGraphIO.Instance().saveChunk(integrityChunk);
    //   App.Instance().getLogger().fine("Unloaded Chunk " + chunk.toString());
    // }
  }

  public SubGraph getChunkGraph(Chunk chunk) {
    SubGraph integrityChunk = loadedChunks.get(chunk);
    if (integrityChunk == null) {
      App.Instance().getLogger().fine("Chunk not loaded! " + chunk.toString());
      loadChunk(chunk);
      integrityChunk = loadedChunks.get(chunk);
    }
    return integrityChunk;
  }

  // Operation queue and status
  private LinkedHashSet<Chunk> chunkUpdateQueue = new LinkedHashSet<>();
  private Set<Chunk> inProgressSingle = new HashSet<>();
  private Set<Chunk> inProgressGroup = new HashSet<>();
  private Set<Chunk> inProgress = Sets.union(inProgressSingle, inProgressGroup);
  private LinkedHashSet<Chunk> chunkDependencyQueue = new LinkedHashSet<>();

  private LinkedHashSet<SuperGraph> chunkGroupQueue = new LinkedHashSet<>();
  private Map<Chunk, SubGraph> loadedChunks = new HashMap<>();

  private void processChunks() {
    processSingleChunks();
    processMultipleChunks();
  }

  private void processMultipleChunks() {

    // Start a new chunk group calculation
    final int MAX_DISTANCE = Config.Instance().getMaxChunkDistance();
    for (Chunk chunk : new ArrayList<>(chunkDependencyQueue)) {
      SubGraph subgraph = getChunkGraph(chunk);
      subgraph.getState().dependantChunks.add(new IntegerXZ(chunk.getX(), chunk.getZ()));
      Set<Chunk> requestedChunks =
          subgraph.getState().dependantChunks.stream()
              .map(chunkCoord -> subgraph.getWorld().getChunkAt(chunkCoord.x, chunkCoord.z))
              .collect(Collectors.toSet());
      // requested chunks must be available
      if (!requestedChunks.stream().allMatch(request -> !inProgress.contains(request))) continue;
      // starting chunk cannot be too close to existing chunk groups
      if (!chunkGroupQueue.stream()
          .allMatch(
              existingGroup ->
                  (MAX_DISTANCE * 2)
                      > Math.max(
                          Math.abs(chunk.getX() - existingGroup.getOriginXZ().x),
                          Math.abs(chunk.getZ() - existingGroup.getOriginXZ().z)))) continue;
      // Create chunk group
      inProgressGroup.addAll(requestedChunks);
      chunkDependencyQueue.removeAll(requestedChunks);
      SuperGraph newGroup = new SuperGraph(subgraph);
      chunkGroupQueue.add(newGroup);
      App.Instance().getLogger().fine("Creating SuperGraph " + newGroup.toString());
    }

    // Continue another iteration of existing chunk group calculation
    final int MAX_GROUP_ITERATIONS = MAX_DISTANCE;
    for (SuperGraph group : new ArrayList<>(chunkGroupQueue)) {
      List<Chunk> requestedChunks =
          group.getState().dependantChunks.stream()
              .map(chunkCoord -> group.getWorld().getChunkAt(chunkCoord.x, chunkCoord.z))
              .collect(Collectors.toList());
      if (!requestedChunks.stream()
          .allMatch(
              chunk ->
                  // In order to enter the next iteration step all chunk subgraphs have to be collected
                  // These chunks cant be in progress unless they are already in the chunk group
                  // The possibility of two SuperGraphs entering deadlock over a chunk the other contains should be
                  // negated by utilizing the max chunk distance (i.e. the max iterations on group chunk calculations)
                  // to prevent groups from being created too close to each other and possibly overlapping.
                  group.getSubgraphGrid().contains(chunk.getX(), chunk.getZ()) || !inProgressSingle.contains(chunk)))
        continue;
      App.Instance().getLogger().fine("Calculating SuperGraph " + group.toString());
      inProgressGroup.addAll(requestedChunks);
      chunkGroupQueue.remove(group);
      AsyncBukkit.doTask(
          () -> {
            List<SubGraph> subgraphs =
                requestedChunks.stream().map(chunk -> getChunkGraph(chunk)).collect(Collectors.toList());
            group.addAll(subgraphs);
            return group.getState();
          },
          (state) -> {
            // If there are no outside chunk dependencies (or max_iterations exceeded), retire this group
            // Otherwise remove and report chunks you don't need and queue another iteration
            if (group.getIteration() >= MAX_GROUP_ITERATIONS
                || state.dependantChunks.stream()
                    .allMatch(chunkCoord -> group.getSubgraphGrid().contains(chunkCoord.x, chunkCoord.z))) {
              Block[] blocks = group.getIntegrityViolations();
              Bukkit.getPluginManager().callEvent(new BlockGravityEvent(blocks));
              group.getSubgraphGrid().values().forEach((subgraph) -> inProgressGroup.remove(subgraph.getChunk()));
              group.removeAll();
            } else {
              /* // This probably has too much overhead
              for (Cell<Integer, Integer, SubGraph> cell : group.getSubgraphGrid().cellSet()) {
                if (!state.dependantChunks.contains(new IntegerXZ(cell.getRowKey(), cell.getColumnKey()))) {
                  SubGraph subgraph = cell.getValue();
                  group.remove(subgraph);
                  subgraph.calculateState();
                  Block[] blocks = subgraph.getIntegrityViolations();
                  Bukkit.getPluginManager().callEvent(new BlockGravityEvent(blocks));
                }
              } */
              // Interestingly, I'm not sure whether to use iterator or the set itself
              chunkGroupQueue.add(group);
            }
          });
    }
  }

  private void processSingleChunks() {
    if (chunkUpdateQueue.size() == 0) return;

    Set<Chunk> shouldUpdate = new HashSet<>(chunkUpdateQueue);
    // Do not work on chunks that are being processed
    shouldUpdate.removeAll(inProgress);
    // Do not work on chunks that are already in queue for multi-chunk operation
    shouldUpdate.removeAll(chunkDependencyQueue);
    // Remove chunks to operate from update queue
    chunkUpdateQueue.removeAll(shouldUpdate);
    // Mark chunks as in progress
    inProgressSingle.addAll(shouldUpdate);

    for (Chunk chunk : shouldUpdate) {
      AsyncBukkit.doTask(
          () -> {
            App.Instance().getLogger().fine("Thread Started");
            SubGraph integrityChunk = getChunkGraph(chunk);
            // Update integrity
            integrityChunk.update(chunk.getChunkSnapshot());
            // Thread & Callback
            return integrityChunk;
          },
          (SubGraph integrityChunk) -> {
            if (integrityChunk.getState().dependantChunks.isEmpty()) {
              // Call gravity event on blocks in chunk
              Block[] blocks = integrityChunk.getIntegrityViolations();
              Bukkit.getPluginManager().callEvent(new BlockGravityEvent(blocks));
              App.Instance().getLogger().fine("Blocks to fall: " + blocks.length);
            } else {
              App.Instance().getLogger().fine("Adding chunk to cross-queue");
              chunkDependencyQueue.add(chunk);
            }
            // Mark chunk as free
            inProgressSingle.remove(chunk);
            App.Instance().getLogger().fine("Thread Finished");
          });
    }
  }

  public void queueChunkUpdate(Chunk chunk) {
    chunkUpdateQueue.add(chunk);
  }

  public void debugResetChunk(Chunk chunk) {
    loadedChunks.put(chunk, new SubGraph(chunk));
  }
}
