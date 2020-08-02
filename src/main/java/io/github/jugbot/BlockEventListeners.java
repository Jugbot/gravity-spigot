package io.github.jugbot;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class BlockEventListeners implements Listener {
  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  void onBlocKBreak(BlockBreakEvent event) {
    System.out.println("Block Break Event");
    Bukkit.getPluginManager().callEvent(new BlockChangeEvent(event.getBlock()));
  }

  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  void onBlocKPlace(BlockPlaceEvent event) {
    System.out.println("Block Place Event");
    Bukkit.getPluginManager().callEvent(new BlockChangeEvent(event.getBlock()));
  }
}