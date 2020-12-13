package io.github.jugbot;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import io.github.jugbot.graph.MaxFlow;
import io.github.jugbot.graph.ReservedID;
import io.github.jugbot.graph.SubGraph;
import io.github.jugbot.graph.SuperGraph;
import io.github.jugbot.graph.Vertex;

public class SuperGraphTest {
  private SuperGraph subject = new SuperGraph();
  private Table<Integer, Integer, SubGraph> subgraphs = HashBasedTable.create();

  @BeforeAll
  public static void createMocks() {
    MockConfig.Instance();
    MockWorld.Reset();
    MockWorld.HEIGHT = 6;
  }

  @BeforeEach
  public void setup() throws Exception {
    subgraphs.put(0, 0, new SubGraph(MockWorld.Instance().getChunkAt(0, 0)));
    subgraphs.put(1, 0, new SubGraph(MockWorld.Instance().getChunkAt(1, 0)));
  }

  @Nested
  class TwoChunks {
    @Test
    public void add00() {
      subject.add(subgraphs.get(0, 0));
      Vertex s = new Vertex(ReservedID.SUPER_SOURCE);
      Vertex t = new Vertex(ReservedID.SUPER_DEST);
      Utils.verifyGraph(subject, s, t);
    }

    @Test
    public void add10() {
      subject.add(subgraphs.get(1, 0));
      Vertex s = new Vertex(ReservedID.SUPER_SOURCE);
      Vertex t = new Vertex(ReservedID.SUPER_DEST);
      Utils.verifyGraph(subject, s, t);
    }

    @Test
    public void addTogether() {
      subject.add(subgraphs.get(0, 0));
      subject.add(subgraphs.get(1, 0));
      Vertex s = new Vertex(ReservedID.SUPER_SOURCE);
      Vertex t = new Vertex(ReservedID.SUPER_DEST);
      Utils.verifyGraph(subject, s, t);
      SubGraph toRemove = subgraphs.get(0, 0);
      subject.remove(toRemove);
      Utils.verifyGraph(toRemove, toRemove.src, toRemove.dest);
      Utils.verifyGraphConnectedOnly(subject, subject.src, subject.dest);
      assertEquals(0, MaxFlow.maxFlow(subject, subject.dists, subject.src, subject.dest));
      float maxFlow = subject.outEdges(subject.src).stream().map(e -> e.f).reduce(0f, (a, b) -> (a + b));
      float maxFlow2 = toRemove.outEdges(toRemove.src).stream().map(e -> e.f).reduce(0f, (a, b) -> (a + b));
      // Reflow on supergraph works
      assertEquals(MockWorld.HEIGHT * (16 * 16), maxFlow);
      subject.edges().stream().forEach(e -> e.f = 0);
      assertEquals(maxFlow, MaxFlow.maxFlow(subject, subject.dists, subject.src, subject.dest));
      // Reflow on removed works
      assertEquals(MockWorld.HEIGHT * (16 * 16), maxFlow2);
      toRemove.edges().stream().forEach(e -> e.f = 0);
      assertEquals(maxFlow2, MaxFlow.maxFlow(toRemove, toRemove.dists, toRemove.src, toRemove.dest));
    }
  }
}
