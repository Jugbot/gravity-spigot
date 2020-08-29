package io.github.jugbot;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class BlockChangeListener implements Listener {

  // TODO: Change to pillars of blocks not single blocks for better handling sand
  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  void onBlockChange(BlockChangeEvent event) {
    App.Instance().getLogger().fine("Block Gravity");
    ChunkProcessor.Instance().queueChunkUpdate(event.getBlock().getChunk());
  }
}
