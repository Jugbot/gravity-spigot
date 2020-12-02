package io.github.jugbot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import io.github.jugbot.graph.GraphState;
import io.github.jugbot.graph.SubGraph;
import io.github.jugbot.util.IntegerXZ;

public class SubGraphTest {

  private SubGraph subject;
  private MockWorld mockedWorld;
  private static MockedStatic<Config> mocked = mockStatic(Config.class);

  @BeforeEach
  public void setup() throws Exception {
    Config config = mock(Config.class);
    mocked.when(Config::Instance).thenReturn(config);
    when(config.getBlockData()).thenReturn(new BlockData());
    MockWorld.HEIGHT = 32;
    mockedWorld = MockWorld.Instance();
    subject = new SubGraph(mockedWorld.getChunkAt(0, 0));
    System.out.println("done");
  }

  @Nested
  class ChunkAtOrigin {
    @Test
    public void cornerNW() throws Exception {
      Block offending = mockedWorld.getBlockAt(0, 128, 0);
      ((MockBlockData) offending.getBlockData()).material = Material.DIRT;
      GraphState state = subject.update(mockedWorld.getChunkAt(0, 0).getChunkSnapshot());

      assertEquals(1, state.offendingNodes.size());
      assertEquals(2, state.dependantChunks.size());
      assertTrue(
          state.dependantChunks.contains(new IntegerXZ(0, -1)) && state.dependantChunks.contains(new IntegerXZ(-1, 0)));

      ((MockBlockData) offending.getBlockData()).material = Material.AIR;
      state = subject.update(mockedWorld.getChunkAt(0, 0).getChunkSnapshot());
      assertEquals(0, state.offendingNodes.size());
      assertEquals(0, state.dependantChunks.size());
    }

    @Test
    public void cornerSE() throws Exception {
      Block offending = mockedWorld.getBlockAt(15, 128, 15);
      ((MockBlockData) offending.getBlockData()).material = Material.DIRT;
      GraphState state = subject.update(mockedWorld.getChunkAt(0, 0).getChunkSnapshot());

      assertEquals(1, state.offendingNodes.size());
      assertEquals(2, state.dependantChunks.size());
      assertTrue(
          state.dependantChunks.contains(new IntegerXZ(0, 1)) && state.dependantChunks.contains(new IntegerXZ(1, 0)));

      ((MockBlockData) offending.getBlockData()).material = Material.AIR;
      state = subject.update(mockedWorld.getChunkAt(0, 0).getChunkSnapshot());
      assertEquals(0, state.offendingNodes.size());
      assertEquals(0, state.dependantChunks.size());

      offending = mockedWorld.getBlockAt(8, 128, 8);
      ((MockBlockData) offending.getBlockData()).material = Material.DIRT;
      state = subject.update(mockedWorld.getChunkAt(0, 0).getChunkSnapshot());
      assertEquals(1, state.offendingNodes.size());
      assertEquals(0, state.dependantChunks.size());
    }
  }
}
