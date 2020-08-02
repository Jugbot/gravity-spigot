package io.github.jugbot;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class BlockGravityListener implements Listener {
  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  void onBlockGravity(BlockGravityEvent event) {
    System.out.println("Block Gravity");
    Block block = event.getBlock();
    Location location = block.getLocation();
    Block below = location.clone().add(0, -1, 0).getBlock();
    // If block can fall, then make it fall
    // Otherwise just break the block
    if (below != null && below.isPassable()) {
      block.getWorld().spawnFallingBlock(location, block.getBlockData());
      block.setType(Material.AIR);
    } else {
      block.breakNaturally();
    }
  }
}