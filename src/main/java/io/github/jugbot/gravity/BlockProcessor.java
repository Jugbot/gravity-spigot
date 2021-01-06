package io.github.jugbot.gravity;

import java.util.Collection;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Set;

import com.google.common.collect.Sets;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import io.github.jugbot.gravity.events.BlockGravityEvent;
import io.github.jugbot.gravity.util.AsyncBukkit;
import io.github.jugbot.gravity.util.PriorityQueueSet;

/**
 * Takes block changes and outputs offending blocks that should fall asynchronously. Note reported blocks may be in
 * other chunks.
 */
public class BlockProcessor {
  private static BlockProcessor instance;
  private LinkedHashSet<Block> blockUpdateQueue = new LinkedHashSet<>();
  private PriorityQueueSet<Block> destructionQueue =
      new PriorityQueueSet<Block>(
          new Comparator<Block>() {
            @Override
            public int compare(Block b1, Block b2) {
              return b1.getY() - b2.getY();
            }
          });

  public static BlockProcessor Instance() {
    if (instance == null) {
      instance = new BlockProcessor();
    }
    return instance;
  }

  /** Fires block processing every tick. */
  private BlockProcessor() {
    Bukkit.getScheduler()
        .scheduleSyncRepeatingTask(
            App.Instance(),
            new Runnable() {
              @Override
              public void run() {
                BlockProcessor.Instance().processBlocks();
              }
            },
            0,
            1);
  }

  private void processBlocks() {
    // Prepare blocks to check
    Set<Block> toUpdate = Sets.newHashSet(blockUpdateQueue);
    blockUpdateQueue.clear();
    // Check blocks
    AsyncBukkit.doTask(
        () -> {
          return blocksDisconnectedFromBedrock(toUpdate);
        },
        (Set<Block> blocks) -> {
          destructionQueue.addAll(blocks);
        });
    // Send blocks to be destroyed
    for (int i = 0; i < Config.Instance().getDestructionPerTick() && !destructionQueue.isEmpty(); i++) {
      Bukkit.getPluginManager().callEvent(new BlockGravityEvent(destructionQueue.remove()));
    }
  }

  private Set<Block> blocksDisconnectedFromBedrock(Collection<Block> starts) {
    Set<Block> connected = new HashSet<>();
    Set<Block> disconnected = new HashSet<>();
    for (Block start : starts) {
      Set<Block> visited = new HashSet<>();
      Deque<Block> stack = new LinkedList<Block>();
      stack.add(start);
      boolean isConnected = false;
      // Depth-first traversal downwards
      while (!stack.isEmpty()) {
        Block candidate = stack.removeLast();
        if (visited.contains(candidate) || !candidate.getType().isSolid()) {
          continue;
        }
        if (disconnected.contains(candidate)) {
          isConnected = false;
          break;
        }
        if (Config.Instance().isRootBlock(candidate) || connected.contains(candidate)) {
          isConnected = true;
          break;
        }
        visited.add(candidate);
        // Add decendants (order matters)
        if (candidate.getY() < 255) {
          stack.add(candidate.getRelative(BlockFace.UP));
        }
        stack.add(candidate.getRelative(BlockFace.NORTH));
        stack.add(candidate.getRelative(BlockFace.EAST));
        stack.add(candidate.getRelative(BlockFace.SOUTH));
        stack.add(candidate.getRelative(BlockFace.WEST));
        if (candidate.getY() > 0) {
          stack.add(candidate.getRelative(BlockFace.DOWN));
        }
      }
      if (isConnected) {
        connected.addAll(visited);
      } else {
        disconnected.addAll(visited);
      }
    }
    return disconnected;
  }

  public void queueBlockUpdate(Block block) {
    if (block.getType().isSolid()) {
      // Block Added
      blockUpdateQueue.add(block);
    } else {
      // Block Removed
      if (block.getY() < 255) {
        blockUpdateQueue.add(block.getRelative(BlockFace.UP));
      }
      if (block.getY() > 0) {
        blockUpdateQueue.add(block.getRelative(BlockFace.DOWN));
      }
      blockUpdateQueue.add(block.getRelative(BlockFace.NORTH));
      blockUpdateQueue.add(block.getRelative(BlockFace.EAST));
      blockUpdateQueue.add(block.getRelative(BlockFace.SOUTH));
      blockUpdateQueue.add(block.getRelative(BlockFace.WEST));
    }
  }
}
