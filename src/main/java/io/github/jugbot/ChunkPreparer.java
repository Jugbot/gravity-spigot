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

public class ChunkPreparer implements FutureCallback<Block[]>, Listener {
  LinkedHashSet<Chunk> chunkUpdateQueue = new LinkedHashSet<Chunk>();
  Set<Chunk> inProgress = new HashSet<Chunk>();
  ListeningExecutorService pool;

  ChunkPreparer() {
    // Currently spawns a max of one thread per chunk
    // Can decrease by setting explicit pool size
    pool = MoreExecutors.listeningDecorator(Executors.newCachedThreadPool());
  }

  @EventHandler
  void onBlockChange(BlockChangeEvent event) {
    chunkUpdateQueue.add(event.getBlock().getChunk());
    Set<Chunk> shouldUpdate = new HashSet<Chunk>(chunkUpdateQueue);
    shouldUpdate.removeAll(inProgress);
    chunkUpdateQueue.removeAll(shouldUpdate);
    for (Chunk chunk : shouldUpdate) {
      StructuralIntegrityChunk maths = new StructuralIntegrityChunk(chunk);
      // ListenableFuture<Block[]> future = pool.submit(maths);
      // Futures.addCallback(future, this, pool);
      StructuralIntegrityChunk.getBrokenBlocks(maths, new StructuralIntegrityChunk.Callback<Block[]>(){

        @Override
        public void done(Block[] blocks) {
          inProgress.remove(chunk);
          for (Block block : blocks) {
            Bukkit.getPluginManager().callEvent(new BlockGravityEvent(block));
          }
        }
        
      });
      System.out.println("Thread Started");
    }
    inProgress.addAll(shouldUpdate);
  }

  @Override
  public void onSuccess(Block[] blocks) {
    System.out.println("Thread Completed");
    // Mark chunk as finished
    // inProgress.remove(((StructuralIntegrityChunk) thread).chunk);
    for (Block block : blocks) {
      Bukkit.getPluginManager().callEvent(new BlockGravityEvent(block));
      // if (block.equals(block.getChunk().getWorld().getBlockAt(block.getLocation()))) {
      // 
      // } else {
      //   System.out.println("Block update invalidated [too late]");
      // }
    }
  }

  @Override
  public void onFailure(Throwable t) {
    System.out.println("Thread Failed");
    System.err.println(t.toString());
  }
}