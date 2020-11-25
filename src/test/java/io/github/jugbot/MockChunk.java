package io.github.jugbot;

import java.util.Collection;

import org.apache.commons.lang.NotImplementedException;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.plugin.Plugin;

public class MockChunk implements Chunk {
  final MockChunkSnapshot snapshot;
  final int x, z;

  public MockChunk(int x, int z) {
    this.x = x;
    this.z = z;
    snapshot = new MockChunkSnapshot();
  }

  @Override
  public int getX() {
    return x;
  }

  @Override
  public int getZ() {
    return z;
  }

  @Override
  public World getWorld() {
    throw new NotImplementedException();
  }

  @Override
  public Block getBlock(int x, int y, int z) {
    return snapshot.getBlock(x, y, z);
  }

  @Override
  public ChunkSnapshot getChunkSnapshot() {
    return snapshot;
  }

  @Override
  public ChunkSnapshot getChunkSnapshot(boolean includeMaxblocky, boolean includeBiome, boolean includeBiomeTempRain) {
    throw new NotImplementedException();
  }

  @Override
  public Entity[] getEntities() {
    throw new NotImplementedException();
  }

  @Override
  public BlockState[] getTileEntities() {
    throw new NotImplementedException();
  }

  @Override
  public boolean isLoaded() {
    throw new NotImplementedException();
  }

  @Override
  public boolean load(boolean generate) {
    throw new NotImplementedException();
  }

  @Override
  public boolean load() {
    throw new NotImplementedException();
  }

  @Override
  public boolean unload(boolean save) {
    throw new NotImplementedException();
  }

  @Override
  public boolean unload() {
    throw new NotImplementedException();
  }

  @Override
  public boolean isSlimeChunk() {
    throw new NotImplementedException();
  }

  @Override
  public boolean isForceLoaded() {
    throw new NotImplementedException();
  }

  @Override
  public void setForceLoaded(boolean forced) {
    throw new NotImplementedException();
  }

  @Override
  public boolean addPluginChunkTicket(Plugin plugin) {
    throw new NotImplementedException();
  }

  @Override
  public boolean removePluginChunkTicket(Plugin plugin) {
    throw new NotImplementedException();
  }

  @Override
  public Collection<Plugin> getPluginChunkTickets() {
    throw new NotImplementedException();
  }

  @Override
  public long getInhabitedTime() {
    throw new NotImplementedException();
  }

  @Override
  public void setInhabitedTime(long ticks) {
    throw new NotImplementedException();
  }

  @Override
  public boolean contains(BlockData block) {
    throw new NotImplementedException();
  }

  @Override
  public PersistentDataContainer getPersistentDataContainer() {
    throw new NotImplementedException();
  }
}
