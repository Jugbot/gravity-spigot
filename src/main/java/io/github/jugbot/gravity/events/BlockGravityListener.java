package io.github.jugbot.gravity.events;

import io.github.jugbot.gravity.Config;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class BlockGravityListener implements Listener {

  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  void onBlockGravity(BlockGravityEvent event) {
    Block block = event.getBlocks();
    if (Config.Instance().blockCanFall(block)) {
      block.getWorld().spawnFallingBlock(block.getLocation().add(0.5, 0.0, 0.5), block.getBlockData());
      block.setType(Material.AIR);
    }
  }
}
