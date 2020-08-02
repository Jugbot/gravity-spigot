package io.github.jugbot;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ChunkPreparer implements TaskListener<Block[]>, Listener {
  LinkedHashSet<Chunk> chunkUpdateQueue = new LinkedHashSet<Chunk>();
  Set<Chunk> inProgress = new HashSet<Chunk>();
  ExecutorService pool;

  ChunkPreparer() {
    // Currently spawns a max of one thread per chunk
    // Can decrease by setting explicit pool size
    pool = Executors.newCachedThreadPool();
  }

  @EventHandler
  void onBlockChange(BlockChangeEvent event) {
    chunkUpdateQueue.add(event.getBlock().getChunk());
    Set<Chunk> shouldUpdate = new HashSet<Chunk>(chunkUpdateQueue);
    shouldUpdate.removeAll(inProgress);
    chunkUpdateQueue.removeAll(shouldUpdate);
    for (Chunk chunk : shouldUpdate) {
      StructuralIntegrityChunk maths = new StructuralIntegrityChunk(chunk);
      maths.addListener(this);
      pool.submit(maths);
      System.out.println("Thread Started");
    }
    inProgress.addAll(shouldUpdate);
  }

  @Override
  public void threadComplete(Callable<Block[]> thread, Block[] blocks) {
    System.out.println("Thread Completed");
    for (Block block : blocks) {
      // Mark chunk as finished
      inProgress.remove(block.getChunk());
      if (block.equals(block.getChunk().getWorld().getBlockAt(block.getLocation()))) {
        Bukkit.getPluginManager().callEvent(new BlockGravityEvent(block));
      } else {
        System.out.println("Block update invalidated [too late]");
      }
    }
  }
}