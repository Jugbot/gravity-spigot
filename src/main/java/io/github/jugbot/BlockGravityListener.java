package io.github.jugbot;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class BlockGravityListener implements Listener {
  // TODO: Changeto pillars of blocks not single blocks for better handling sand
  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  void onBlockGravity(BlockGravityEvent event) {
    System.out.println("Block Gravity");
    for (Block block : event.getBlocks()) {
      Location location = block.getLocation();
      Block below = block.getRelative(0, -1, 0);
      // If block can fall, then make it fall
      // Otherwise just break the block
      if (false && below != null && below.isPassable()) {
        block.getWorld().spawnFallingBlock(location.add(0.5, 0.5, 0.5), block.getBlockData());
        block.setType(Material.AIR);
      } else {
        block.breakNaturally();
      }
    }
  }
}