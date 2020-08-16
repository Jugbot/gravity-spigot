package io.github.jugbot;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;

/**
 * Takes block changes and outputs offending blocks that should fall asynchronously Note reported blocks may be in other
 * chunks
 */
public class ChunkProcessor {
  private static ChunkProcessor instance;
  // Operation queue and status
  // TODO: Change to Map<Chunk, List<Block>> to allow queueing block changes
  LinkedHashSet<Chunk> chunkUpdateQueue = new LinkedHashSet<Chunk>();
  Set<Chunk> inProgress = new HashSet<Chunk>();
  // Loaded chunks
  Map<Chunk, IntegrityChunk> loadedChunks = new HashMap<>();

  public static ChunkProcessor Instance() {
    if (instance == null) instance = new ChunkProcessor();
    return instance;
  }

  /**
   * Load IntegrityChunk alongside Chunk
   *
   * @see ChunkListener
   */
  public void loadChunk(Chunk chunk) {
    IntegrityChunk integrityChunk = loadedChunks.get(chunk);
    if (integrityChunk == null) {
      integrityChunk =
          IntegrityChunkStorage.Instance().loadChunk(chunk.getWorld().getName(), chunk.getX(), chunk.getZ());
    }
    if (integrityChunk == null) {
      integrityChunk = new IntegrityChunk(chunk);
      IntegrityChunkStorage.Instance().saveChunk(integrityChunk);
    }
    loadedChunks.put(chunk, integrityChunk);
  }

  /**
   * Save IntegrityChunk alongside Chunk
   *
   * @see ChunkListener
   */
  public void unloadChunk(Chunk chunk) {
    IntegrityChunk integrityChunk = loadedChunks.remove(chunk);
    if (integrityChunk != null) {
      IntegrityChunkStorage.Instance().saveChunk(integrityChunk);
    }
  }

  public void processChunk(Chunk chunk) {
    chunkUpdateQueue.add(chunk);
    processChunks();
  }

  private void processChunks() {
    // Make set of dirty chunks that are not being processed
    Set<Chunk> shouldUpdate = new HashSet<Chunk>(chunkUpdateQueue);
    shouldUpdate.removeAll(inProgress);
    // Remove chunks to operate from update queue
    chunkUpdateQueue.removeAll(shouldUpdate);
    // Mark chunks as in progress
    inProgress.addAll(shouldUpdate);

    for (Chunk chunk : shouldUpdate) {
      IntegrityChunk integrityChunk = loadedChunks.get(chunk); // new IntegrityChunk(chunk);
      // Just in case
      if (integrityChunk == null) {
        System.out.println("Chunk not loaded! " + chunk.toString());
        loadChunk(chunk);
        integrityChunk = loadedChunks.get(chunk);
      }
      // Thread & Callback
      integrityChunk.getBrokenBlocks(
          new IntegrityChunk.Callback<Location[]>() {
            @Override
            public void done(Location[] locations) {
              // Mark chunk as free
              inProgress.remove(chunk);
              Block[] blocks = Arrays.asList(locations).stream().map(loc -> loc.getBlock()).toArray(Block[]::new);
              // Call gravity event on blocks in chunk
              Bukkit.getPluginManager().callEvent(new BlockGravityEvent(blocks));
              System.out.println("Thread Finished");
              System.out.println("Blocks to fall: " + blocks.length);
            }
          });
      System.out.println("Thread Started");
    }
  }
}
