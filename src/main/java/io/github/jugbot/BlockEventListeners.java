package io.github.jugbot;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockPistonEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.block.EntityBlockFormEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

public class BlockEventListeners implements Listener {

  private void delayedBlockEvent(Block block) {
    // Wait until after block is broken/placed to make sure the material change is resolved
    App.Instance()
        .getServer()
        .getScheduler()
        .scheduleSyncDelayedTask(
            App.Instance(),
            new Runnable() {
              public void run() {
                Bukkit.getPluginManager().callEvent(new BlockChangeEvent(block));
              }
            });
  }

  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  void onBlockUpdate(BlockBreakEvent event) {
    delayedBlockEvent(event.getBlock());
  }

  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  void onBlockUpdate(BlockPlaceEvent event) {
    delayedBlockEvent(event.getBlock());
  }

  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  void onBlockUpdate(BlockBurnEvent event) {
    delayedBlockEvent(event.getBlock());
  }

  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  void onBlockUpdate(BlockExplodeEvent event) {
    for (Block block : event.blockList()) {
      delayedBlockEvent(block);
    }
  }

  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  void onBlockUpdate(EntityExplodeEvent event) {
    for (Block block : event.blockList()) {
      delayedBlockEvent(block);
    }
  }

  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  void onBlockUpdate(BlockFadeEvent event) {
    delayedBlockEvent(event.getBlock());
  }

  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  void onBlockUpdate(BlockFormEvent event) {
    delayedBlockEvent(event.getBlock());
  }

  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  void onBlockUpdate(EntityBlockFormEvent event) {
    delayedBlockEvent(event.getBlock());
  }

  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  void onBlockUpdate(BlockSpreadEvent event) {
    delayedBlockEvent(event.getBlock());
  }

  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  void onBlockUpdate(BlockPistonRetractEvent event) {
    for (Block block : event.getBlocks()) {
      delayedBlockEvent(block);
    }
  }

  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  void onBlockUpdate(BlockPistonExtendEvent event) {
    for (Block block : event.getBlocks()) {
      delayedBlockEvent(block);
    }
  }

  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  void onBlockUpdate(BlockGrowEvent event) {
    delayedBlockEvent(event.getBlock());
  }

  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  void onBlockUpdate(LeavesDecayEvent event) {
    delayedBlockEvent(event.getBlock());
  }
}
