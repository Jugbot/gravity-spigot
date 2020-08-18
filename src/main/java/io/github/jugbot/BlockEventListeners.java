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
    System.out.println("Block Break Event");
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
    System.out.println("Block Place Event");
    Bukkit.getPluginManager().callEvent(new BlockChangeEvent(event.getBlock()));
  }

  // @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  // void onBlockBurn(BlockBurnEvent event) {
  //   System.out.println("Block Place Event");
  //   Bukkit.getPluginManager().callEvent(new BlockChangeEvent(event.getBlock()));
  // }

  // @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  // void onBlockExplode(BlockExplodeEvent event) {
  //   System.out.println("Block Place Event");
  //   Bukkit.getPluginManager().callEvent(new BlockChangeEvent(event.getBlock()));
  // }

  // @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  // void onBlockFade(BlockFadeEvent event) {
  //   System.out.println("Block Place Event");
  //   Bukkit.getPluginManager().callEvent(new BlockChangeEvent(event.getBlock()));
  // }

  // @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  // void onBlockBurn(BlockPistonEvent event) {
  //   System.out.println("Block Place Event");
  //   Bukkit.getPluginManager().callEvent(new BlockChangeEvent(event.getBlock()));
  // }

  // @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  // void onBlockBurn(BlockGrowEvent event) {
  //   System.out.println("Block Place Event");
  //   Bukkit.getPluginManager().callEvent(new BlockChangeEvent(event.getBlock()));
  // }

  // @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  // void onBlockBurn(LeavesDecayEvent event) {
  //   System.out.println("Block Place Event");
  //   Bukkit.getPluginManager().callEvent(new BlockChangeEvent(event.getBlock()));
  // }

  // @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  // void onBlockBurn(EntityChangeBlockEvent event) {
  //   System.out.println("Block Place Event");
  //   Bukkit.getPluginManager().callEvent(new BlockChangeEvent(event.getBlock()));
  // }
}
