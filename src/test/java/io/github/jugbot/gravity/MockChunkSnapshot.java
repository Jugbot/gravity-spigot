package io.github.jugbot.gravity;

import org.apache.commons.lang.NotImplementedException;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.data.BlockData;

public class MockChunkSnapshot implements ChunkSnapshot {
  // feel free to modify
  public BlockData[][][] blocks = new MockBlockData[16][256][16];

  public MockChunkSnapshot(BlockData[][][] blocks) {
    this.blocks = blocks;
  }

  @Override
  public boolean contains(BlockData arg0) {
    throw new NotImplementedException();
    // return false;
  }

  @Override
  public Biome getBiome(int arg0, int arg1) {
    throw new NotImplementedException();
    // return null;
  }

  @Override
  public Biome getBiome(int arg0, int arg1, int arg2) {
    throw new NotImplementedException();
    // return null;
  }

  @Override
  public BlockData getBlockData(int x, int y, int z) {
    return blocks[x][y][z];
    // return null;
  }

  @Override
  public int getBlockEmittedLight(int arg0, int arg1, int arg2) {
    throw new NotImplementedException();
    // return 0;
  }

  @Override
  public int getBlockSkyLight(int arg0, int arg1, int arg2) {
    throw new NotImplementedException();
    // return 0;
  }

  @Override
  public Material getBlockType(int x, int y, int z) {
    return blocks[x][y][z].getMaterial();
  }

  @Override
  public long getCaptureFullTime() {
    throw new NotImplementedException();
    // return 0;
  }

  @Override
  public int getData(int arg0, int arg1, int arg2) {
    throw new NotImplementedException();
    // return 0;
  }

  @Override
  public int getHighestBlockYAt(int arg0, int arg1) {
    throw new NotImplementedException();
    // return 0;
  }

  @Override
  public double getRawBiomeTemperature(int arg0, int arg1) {
    throw new NotImplementedException();
    // return 0;
  }

  @Override
  public double getRawBiomeTemperature(int arg0, int arg1, int arg2) {
    throw new NotImplementedException();
    // return 0;
  }

  @Override
  public String getWorldName() {
    throw new NotImplementedException();
    // return null;
  }

  @Override
  public int getX() {
    throw new NotImplementedException();
    // return 0;
  }

  @Override
  public int getZ() {
    throw new NotImplementedException();
    // return 0;
  }

  @Override
  public boolean isSectionEmpty(int arg0) {
    throw new NotImplementedException();
    // return false;
  }
}
