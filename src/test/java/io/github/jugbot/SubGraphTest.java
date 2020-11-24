package io.github.jugbot;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.WorldMock;
import io.github.jugbot.graph.SubGraph;

public class SubGraphTest {
  private ServerMock server;
  private App plugin;
  private World world;

  @Before
  public void setUp() {
    server = MockBukkit.mock();
    plugin = (App) MockBukkit.load(App.class);
    world = new WorldMock(Material.GRASS, 2);
  }

  @After
  public void tearDown() {
    MockBukkit.unmock();
  }

  @Test
  public void mockedChunk() {
    Chunk chunk = world.getChunkAt(0, 0);
    new SubGraph(chunk);
  }
}
