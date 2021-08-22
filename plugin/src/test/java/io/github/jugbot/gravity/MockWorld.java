package io.github.jugbot.gravity;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import org.apache.commons.lang.NotImplementedException;
import org.bukkit.BlockChangeDelegate;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Difficulty;
import org.bukkit.Effect;
import org.bukkit.FluidCollisionMode;
import org.bukkit.GameRule;
import org.bukkit.HeightMap;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Raid;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.StructureType;
import org.bukkit.TreeType;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.WorldType;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.boss.DragonBattle;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Item;
import org.bukkit.entity.LightningStrike;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Consumer;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

public abstract class MockWorld implements World {
  public static int HEIGHT = 0;

  static MockWorld instance = null;

  Table<Integer, Integer, Chunk> chunkmap = HashBasedTable.create();

  private MockWorld() {}

  public static MockWorld Instance() {
    if (instance == null) instance = Mocker.newMock(MockWorld.class);
    return instance;
  }

  public static void Reset() {
    instance = null;
    HEIGHT = 0;
  }

  @Override
  public Block getBlockAt(int x, int y, int z) {
    // Chunk optional = chunkmap.get(Math.floorDiv(x, 16), Math.floorDiv(z, 16));
    // if (optional == null) return new MockBlock(new MockBlockData(Material.AIR), x, y, z);
    return getChunkAt(Math.floorDiv(x, 16), Math.floorDiv(z, 16)).getBlock(x % 16, y, z % 16);
  }

  @Override
  public Chunk getChunkAt(int x, int z) {
    if (!chunkmap.contains(x, z)) {
      chunkmap.put(x, z, new MockChunk(x, z));
    }
    return chunkmap.get(x, z);
  }
}
