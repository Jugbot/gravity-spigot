package io.github.jugbot;

import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.NotImplementedException;
import org.bukkit.Chunk;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.PistonMoveReaction;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

public class MockBlock implements Block {
  BlockData blockData;

  public MockBlock(BlockData mat) {
    this.blockData = mat;
  }

  @Override
  public void setMetadata(String metadataKey, MetadataValue newMetadataValue) {
    throw new NotImplementedException();
  }

  @Override
  public List<MetadataValue> getMetadata(String metadataKey) {
    throw new NotImplementedException();
    // return null;
  }

  @Override
  public boolean hasMetadata(String metadataKey) {
    throw new NotImplementedException();
    // return false;
  }

  @Override
  public void removeMetadata(String metadataKey, Plugin owningPlugin) {
    throw new NotImplementedException();
  }

  @Override
  public byte getData() {
    throw new NotImplementedException();
    // return 0;
  }

  @Override
  public BlockData getBlockData() {
    throw new NotImplementedException();
    // return null;
  }

  @Override
  public Block getRelative(int modX, int modY, int modZ) {
    return new MockBlock(null);
  }

  @Override
  public Block getRelative(BlockFace face) {
    throw new NotImplementedException();
    // return null;
  }

  @Override
  public Block getRelative(BlockFace face, int distance) {
    throw new NotImplementedException();
    // return null;
  }

  @Override
  public Material getType() {
    throw new NotImplementedException();
    // return null;
  }

  @Override
  public byte getLightLevel() {
    throw new NotImplementedException();
    // return 0;
  }

  @Override
  public byte getLightFromSky() {
    throw new NotImplementedException();
    // return 0;
  }

  @Override
  public byte getLightFromBlocks() {
    throw new NotImplementedException();
    // return 0;
  }

  @Override
  public World getWorld() {
    throw new NotImplementedException();
    // return null;
  }

  @Override
  public int getX() {
    throw new NotImplementedException();
    // return 0;
  }

  @Override
  public int getY() {
    throw new NotImplementedException();
    // return 0;
  }

  @Override
  public int getZ() {
    throw new NotImplementedException();
    // return 0;
  }

  @Override
  public Location getLocation() {
    throw new NotImplementedException();
    // return null;
  }

  @Override
  public Location getLocation(Location loc) {
    throw new NotImplementedException();
    // return null;
  }

  @Override
  public Chunk getChunk() {
    throw new NotImplementedException();
    // return null;
  }

  @Override
  public void setBlockData(BlockData data) {
    throw new NotImplementedException();
  }

  @Override
  public void setBlockData(BlockData data, boolean applyPhysics) {
    throw new NotImplementedException();
  }

  @Override
  public void setType(Material type) {
    throw new NotImplementedException();
  }

  @Override
  public void setType(Material type, boolean applyPhysics) {
    throw new NotImplementedException();
  }

  @Override
  public BlockFace getFace(Block block) {
    throw new NotImplementedException();
    // return null;
  }

  @Override
  public BlockState getState() {
    throw new NotImplementedException();
    // return null;
  }

  @Override
  public Biome getBiome() {
    throw new NotImplementedException();
    // return null;
  }

  @Override
  public void setBiome(Biome bio) {
    throw new NotImplementedException();
  }

  @Override
  public boolean isBlockPowered() {
    throw new NotImplementedException();
    // return false;
  }

  @Override
  public boolean isBlockIndirectlyPowered() {
    throw new NotImplementedException();
    // return false;
  }

  @Override
  public boolean isBlockFacePowered(BlockFace face) {
    throw new NotImplementedException();
    // return false;
  }

  @Override
  public boolean isBlockFaceIndirectlyPowered(BlockFace face) {
    throw new NotImplementedException();
    // return false;
  }

  @Override
  public int getBlockPower(BlockFace face) {
    throw new NotImplementedException();
    // return 0;
  }

  @Override
  public int getBlockPower() {
    throw new NotImplementedException();
    // return 0;
  }

  @Override
  public boolean isEmpty() {
    throw new NotImplementedException();
    // return false;
  }

  @Override
  public boolean isLiquid() {
    throw new NotImplementedException();
    // return false;
  }

  @Override
  public double getTemperature() {
    throw new NotImplementedException();
    // return 0;
  }

  @Override
  public double getHumidity() {
    throw new NotImplementedException();
    // return 0;
  }

  @Override
  public PistonMoveReaction getPistonMoveReaction() {
    throw new NotImplementedException();
    // return null;
  }

  @Override
  public boolean breakNaturally() {
    throw new NotImplementedException();
    // return false;
  }

  @Override
  public boolean breakNaturally(ItemStack tool) {
    throw new NotImplementedException();
    // return false;
  }

  @Override
  public Collection<ItemStack> getDrops() {
    throw new NotImplementedException();
    // return null;
  }

  @Override
  public Collection<ItemStack> getDrops(ItemStack tool) {
    throw new NotImplementedException();
    // return null;
  }

  @Override
  public Collection<ItemStack> getDrops(ItemStack tool, Entity entity) {
    throw new NotImplementedException();
    // return null;
  }

  @Override
  public boolean isPassable() {
    throw new NotImplementedException();
    // return false;
  }

  @Override
  public RayTraceResult rayTrace(
      Location start, Vector direction, double maxDistance, FluidCollisionMode fluidCollisionMode) {
    throw new NotImplementedException();
    // return null;
  }

  @Override
  public BoundingBox getBoundingBox() {
    throw new NotImplementedException();
    // return null;
  }

  @Override
  public boolean applyBoneMeal(BlockFace arg0) {
    throw new NotImplementedException();
    // return false;
  }
}
