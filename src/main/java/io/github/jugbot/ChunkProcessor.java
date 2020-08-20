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

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;

import io.github.jugbot.util.AsyncBukkit;

/**
 * Takes block changes and outputs offending blocks that should fall asynchronously Note reported blocks may be in other
 * chunks
 */
public class ChunkProcessor {
  private static ChunkProcessor instance;
  // Operation queue and status
  // TODO: Change to Map<Chunk, List<Block>> to allow queueing block changes
  LinkedHashMap<Chunk, List<Block>> chunkUpdateQueue = new LinkedHashMap<>();
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
      System.out.println("Created Chunk " + chunk.toString());
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
      System.out.println("Unloaded Chunk " + chunk.toString());
    }
  }

  public IntegrityChunk getChunk(Chunk chunk) {
    IntegrityChunk integrityChunk = loadedChunks.get(chunk);
    if (integrityChunk == null) {
      System.out.println("Chunk not loaded! " + chunk.toString());
      loadChunk(chunk);
      integrityChunk = loadedChunks.get(chunk);
    }
    return integrityChunk;
  }

  public void processBlock(Block block) {
    Chunk chunk = block.getChunk();
    List<Block> blocks = chunkUpdateQueue.get(chunk);
    if (blocks == null) {
      blocks = new ArrayList<>();
      chunkUpdateQueue.put(chunk, blocks);
    }
    blocks.add(block);
    // Currently just launch a thread whenever there are changes but maybe there is
    // a better way
    processChunks();
  }

  private void processChunks() {
    Map<Chunk, List<Block>> shouldUpdate = new HashMap<>(chunkUpdateQueue);
    shouldUpdate.keySet().removeAll(inProgress);
    // Remove chunks to operate from update queue
    chunkUpdateQueue.keySet().removeAll(shouldUpdate.keySet());
    // Mark chunks as in progress
    inProgress.addAll(shouldUpdate.keySet());

    for (Chunk chunk : shouldUpdate.keySet()) {
      AsyncBukkit.doTask(
          () -> {
            System.out.println("Thread Started");
            IntegrityChunk integrityChunk = getChunk(chunk);
            // Update integrity
            integrityChunk.update(chunk.getChunkSnapshot());
            // Thread & Callback
            return integrityChunk.getIntegrityViolations();
          },
          (Location[] locations) -> {
            // Mark chunk as free
            inProgress.remove(chunk);
            Block[] blocks = Arrays.asList(locations).stream().map(loc -> loc.getBlock()).toArray(Block[]::new);
            // Call gravity event on blocks in chunk
            Bukkit.getPluginManager().callEvent(new BlockGravityEvent(blocks));
            System.out.println("Thread Finished");
            System.out.println("Blocks to fall: " + blocks.length);
          });
    }
  }
}
