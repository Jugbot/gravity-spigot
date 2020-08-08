package io.github.jugbot;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;

public class BlockEventListeners implements Listener {
  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  void onBlockBreak(BlockBreakEvent event) {
    System.out.println("Block Break Event");
    Bukkit.getPluginManager().callEvent(new BlockChangeEvent(event.getBlock()));
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