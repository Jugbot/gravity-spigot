package io.github.jugbot;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.block.Block;

import io.github.jugbot.graph.SubGraph;
import io.github.jugbot.graph.SuperGraph;
import io.github.jugbot.util.AsyncBukkit;

/**
 * Takes block changes and outputs offending blocks that should fall asynchronously. Note reported blocks may be in
 * other chunks.
 */
public class ChunkProcessor {
  private static ChunkProcessor instance;
  // Operation queue and status
  private LinkedHashSet<Chunk> chunkUpdateQueue = new LinkedHashSet<>();
  private Set<Chunk> inProgress = new HashSet<>();
  private LinkedHashSet<SuperGraph> crossChunkUpdateQueue = new LinkedHashSet<>();
  private Map<Chunk, SubGraph> loadedChunks = new HashMap<>();

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

  private void processChunks() {
    if (chunkUpdateQueue.size() == 0) return;

    Set<Chunk> shouldUpdate = new HashSet<>(chunkUpdateQueue);
    // Do not work on chunks that are being processed
    shouldUpdate.removeAll(inProgress);
    // Remove chunks to operate from update queue
    chunkUpdateQueue.removeAll(shouldUpdate);
    // Mark chunks as in progress
    inProgress.addAll(shouldUpdate);

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
            }
            // Mark chunk as free
            inProgress.remove(chunk);
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
