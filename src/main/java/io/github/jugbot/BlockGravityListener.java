package io.github.jugbot;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class BlockGravityListener implements Listener {
  
  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  void onBlockGravity(BlockGravityEvent event) {
    App.Instance().getLogger().fine("Block Gravity");

    for (Block block : event.getBlocks()) {
      App.Instance().getLogger().fine(block.toString());
      Block below = block.getRelative(0, -1, 0);
      // If block can fall, then make it fall
      // Otherwise just break the block
      if (below != null && below.isPassable()) {
        block.getWorld().spawnFallingBlock(block.getLocation().add(0.5, 0.0, 0.5), block.getBlockData());
        block.setType(Material.AIR);
      } else {
        block.breakNaturally();
      }
    }
  }
}
