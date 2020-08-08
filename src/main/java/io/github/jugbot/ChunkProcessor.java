package io.github.jugbot;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

public class ChunkProcessor {
  private static ChunkProcessor instance;
  LinkedHashSet<Chunk> chunkUpdateQueue = new LinkedHashSet<Chunk>();
  Set<Chunk> inProgress = new HashSet<Chunk>();

  public static ChunkProcessor Instance() {
    if (instance == null)
      instance = new ChunkProcessor();
    return instance;
  }

  public void loadIntegrityChunk(Chunk chunk) {
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
      // TODO store data
      IntegrityChunk maths = new IntegrityChunk(chunk);
      // Thread & Callback
      IntegrityChunk.getBrokenBlocks(maths, new IntegrityChunk.Callback<Block[]>(){
        @Override
        public void done(Block[] blocks) {
          // Mark chunk as free
          inProgress.remove(chunk);
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