package io.github.jugbot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import io.github.jugbot.graph.GraphState;
import io.github.jugbot.graph.SubGraph;
import io.github.jugbot.util.IntegerXZ;

public class SubGraphTest {
  private SubGraph subject;

  @BeforeAll
  public static void createMocks() {
    MockConfig.Instance();
    MockWorld.Reset();
    MockWorld.HEIGHT = 32;
  }

  static void assertGraphStructure(SubGraph subject) {
    int specialNodes = 10;
    int chunkNodes = 256 * 16 * 16;
    int V = chunkNodes + specialNodes;
    assertEquals(V, subject.nodes().size());
    // count undirected, multipied by 2
    int srcEdges = chunkNodes + 4;
    int destEdges = 16 * 16 + 4;
    int blockEdges = chunkNodes * 3 - (16 * 16 + 2 * 16 * 256);
    int sideEdges = (16 * 256) * 2 * 4;
    int E = (blockEdges + sideEdges + srcEdges + destEdges) * 2;
    assertEquals(E, subject.edges().size());
  }

  @Nested
  class ChunkAtOrigin {
    @BeforeEach
    public void setup() throws Exception {
      subject = new SubGraph(MockWorld.Instance().getChunkAt(0, 0));
    }

    @Test
    public void expectedSize() {
      assertGraphStructure(subject);
    }

    @Test
    public void cornerNW() throws Exception {
      Block offending = MockWorld.Instance().getBlockAt(0, 128, 0);
      ((MockBlockData) offending.getBlockData()).material = Material.DIRT;
      GraphState state = subject.update(MockWorld.Instance().getChunkAt(0, 0).getChunkSnapshot());

      assertEquals(1, state.offendingNodes.size());
      assertEquals(2, state.dependantChunks.size());
      assertTrue(
          state.dependantChunks.contains(new IntegerXZ(0, -1)) && state.dependantChunks.contains(new IntegerXZ(-1, 0)));

      ((MockBlockData) offending.getBlockData()).material = Material.AIR;
      state = subject.update(MockWorld.Instance().getChunkAt(0, 0).getChunkSnapshot());
      assertEquals(0, state.offendingNodes.size());
      assertEquals(0, state.dependantChunks.size());
    }

    @Test
    public void cornerSE() throws Exception {
      Block offending = MockWorld.Instance().getBlockAt(15, 128, 15);
      ((MockBlockData) offending.getBlockData()).material = Material.DIRT;
      GraphState state = subject.update(MockWorld.Instance().getChunkAt(0, 0).getChunkSnapshot());

      assertEquals(1, state.offendingNodes.size());
      assertEquals(2, state.dependantChunks.size());
      assertTrue(
          state.dependantChunks.contains(new IntegerXZ(0, 1)) && state.dependantChunks.contains(new IntegerXZ(1, 0)));

      ((MockBlockData) offending.getBlockData()).material = Material.AIR;
      state = subject.update(MockWorld.Instance().getChunkAt(0, 0).getChunkSnapshot());
      assertEquals(0, state.offendingNodes.size());
      assertEquals(0, state.dependantChunks.size());

      offending = MockWorld.Instance().getBlockAt(8, 128, 8);
      ((MockBlockData) offending.getBlockData()).material = Material.DIRT;
      state = subject.update(MockWorld.Instance().getChunkAt(0, 0).getChunkSnapshot());
      assertEquals(1, state.offendingNodes.size());
      assertEquals(0, state.dependantChunks.size());
    }
  }
}
