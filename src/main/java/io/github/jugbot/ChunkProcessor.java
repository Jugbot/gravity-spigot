package io.github.jugbot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;

import io.github.jugbot.graph.SubGraph;
import io.github.jugbot.graph.SubGraphIO;
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
  // Loaded chunks
  // TODO: use SoftReference cache for flexible memory use
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
      App.Instance().getLogger().fine("Created Chunk " + chunk.toString());
      integrityChunk = new SubGraph(chunk);
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

  public void debugResetChunk(Chunk chunk) {
    loadedChunks.put(chunk, new SubGraph(chunk));
  }

  public SubGraph getChunk(Chunk chunk) {
    SubGraph integrityChunk = loadedChunks.get(chunk);
    if (integrityChunk == null) {
      App.Instance().getLogger().fine("Chunk not loaded! " + chunk.toString());
      loadChunk(chunk);
      integrityChunk = loadedChunks.get(chunk);
    }
    return integrityChunk;
  }

  public void queueChunkUpdate(Chunk chunk) {
    chunkUpdateQueue.add(chunk);
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
            SubGraph integrityChunk = getChunk(chunk);
            // Update integrity
            integrityChunk.update(chunk.getChunkSnapshot());
            // integrityChunk = new SubGraph(chunk);
            // Thread & Callback
            return integrityChunk.getIntegrityViolations();
          },
          (Block[] blocks) -> {
            // Mark chunk as free
            inProgress.remove(chunk);
            // Call gravity event on blocks in chunk
            Bukkit.getPluginManager().callEvent(new BlockGravityEvent(blocks));
            App.Instance().getLogger().fine("Thread Finished");
            App.Instance().getLogger().fine("Blocks to fall: " + blocks.length);
          });
    }
  }
}
