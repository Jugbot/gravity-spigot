package io.github.jugbot;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class BlockListener<T extends BlockEvent> implements Listener {

  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  void onBlock(T event) {
    System.out.println("Block Event");
    Bukkit.getPluginManager().callEvent(new BlockChangeEvent(event.getBlock()));
  }

  public static <Z extends BlockEvent> BlockListener<Z> getBlockEventListener(Class<Z> clazz) {
    System.out.println("Registering listener for " + clazz.getCanonicalName());
    return new BlockListener<Z>();
  }
}
