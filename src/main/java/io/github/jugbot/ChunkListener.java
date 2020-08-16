package io.github.jugbot;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkPopulateEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

public class ChunkListener implements Listener {
  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  void onChunkLoad(ChunkLoadEvent event) {
    if (event.isNewChunk()) return;
    ChunkProcessor.Instance().loadChunk(event.getChunk());
  }

  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  void onChunkPopulate(ChunkPopulateEvent event) {
    ChunkProcessor.Instance().loadChunk(event.getChunk());
  }

  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  void onChunkLoad(ChunkUnloadEvent event) {
    ChunkProcessor.Instance().unloadChunk(event.getChunk());
  }
}
