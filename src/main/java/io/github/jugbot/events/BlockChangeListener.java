package io.github.jugbot.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import io.github.jugbot.App;
import io.github.jugbot.BlockProcessor;

public class BlockChangeListener implements Listener {

  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  void onBlockChange(BlockChangeEvent event) {
    App.Instance().getLogger().fine("Block Gravity");
    // ChunkProcessor.Instance().queueChunkUpdate(event.getBlock().getChunk());
    BlockProcessor.Instance().queueBlockUpdate(event.getBlock());
  }
}
