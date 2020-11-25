package io.github.jugbot;

import org.apache.commons.lang.NotImplementedException;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;

public class MockChunkSnapshot implements ChunkSnapshot {
  Block[][][] blocks = new MockBlock[16][256][16];

  public MockChunkSnapshot() {
    final int height = 3;
    for (int y = 0; y < 256; y++) {
      for (int x = 0; x < 16; x++) {
        for (int z = 0; z < 16; z++) {
          if (y < height) {
            blocks[x][y][z] = new MockBlock(new MockBlockData(Material.DIRT), x, y, z);
          } else {
            blocks[x][y][z] = new MockBlock(new MockBlockData(Material.AIR), x, y, z);
          }
        }
      }
    }
  }

  public Block getBlock(int x, int y, int z) {
    return blocks[x][y][z];
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
    return blocks[x][y][z].getBlockData();
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
    return blocks[x][y][z].getBlockData().getMaterial();
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
