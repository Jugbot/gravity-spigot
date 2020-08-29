package io.github.jugbot;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class BlockEventListeners implements Listener {
  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  void onBlockBreak(BlockBreakEvent event) {
    App.Instance().getLogger().fine("Block Break Event");
    // Wait until after block is broken
    App.Instance()
        .getServer()
        .getScheduler()
        .scheduleSyncDelayedTask(
            App.Instance(),
            new Runnable() {
              public void run() {
                Bukkit.getPluginManager().callEvent(new BlockChangeEvent(event.getBlock()));
              }
            });
  }

  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  void onBlockPlace(BlockPlaceEvent event) {
    App.Instance().getLogger().fine("Block Place Event");
    // Wait until after block is placed
    App.Instance()
        .getServer()
        .getScheduler()
        .scheduleSyncDelayedTask(
            App.Instance(),
            new Runnable() {
              public void run() {
                Bukkit.getPluginManager().callEvent(new BlockChangeEvent(event.getBlock()));
              }
            });
  }

  // @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  // void onBlockBurn(BlockBurnEvent event) {
  // App.Instance().getLogger().fine("Block Place Event");
  // Bukkit.getPluginManager().callEvent(new BlockChangeEvent(event.getBlock()));
  // }

  // @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  // void onBlockExplode(BlockExplodeEvent event) {
  // App.Instance().getLogger().fine("Block Place Event");
  // Bukkit.getPluginManager().callEvent(new BlockChangeEvent(event.getBlock()));
  // }

  // @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  // void onBlockFade(BlockFadeEvent event) {
  // App.Instance().getLogger().fine("Block Place Event");
  // Bukkit.getPluginManager().callEvent(new BlockChangeEvent(event.getBlock()));
  // }

  // @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  // void onBlockBurn(BlockPistonEvent event) {
  // App.Instance().getLogger().fine("Block Place Event");
  // Bukkit.getPluginManager().callEvent(new BlockChangeEvent(event.getBlock()));
  // }

  // @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  // void onBlockBurn(BlockGrowEvent event) {
  // App.Instance().getLogger().fine("Block Place Event");
  // Bukkit.getPluginManager().callEvent(new BlockChangeEvent(event.getBlock()));
  // }

  // @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  // void onBlockBurn(LeavesDecayEvent event) {
  // App.Instance().getLogger().fine("Block Place Event");
  // Bukkit.getPluginManager().callEvent(new BlockChangeEvent(event.getBlock()));
  // }

  // @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  // void onBlockBurn(EntityChangeBlockEvent event) {
  // App.Instance().getLogger().fine("Block Place Event");
  // Bukkit.getPluginManager().callEvent(new BlockChangeEvent(event.getBlock()));
  // }
}
