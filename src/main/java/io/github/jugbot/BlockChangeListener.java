package io.github.jugbot;

import javax.annotation.Nonnull;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class BlockChangeListener implements Listener {

  // TODO: Changeto pillars of blocks not single blocks for better handling sand
  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  void onBlockChange(BlockChangeEvent event) {
    System.out.println("Block Gravity");
    ChunkProcessor.Instance().loadIntegrityChunk(event.getBlock().getChunk());
  }
}