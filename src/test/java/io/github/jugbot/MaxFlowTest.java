package io.github.jugbot;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;
import org.junit.jupiter.api.Test;

import io.github.jugbot.util.MaxFlow;

public class MaxFlowTest {

  @Test
  public void returnsMaxFlow() {
    List<MaxFlow.Edge>[] graph = MaxFlow.createGraph(3);
    MaxFlow.createEdge(graph, 0, 1, 3);
    MaxFlow.createEdge(graph, 0, 2, 2);
    MaxFlow.createEdge(graph, 1, 2, 2);
    // int[] dist = new int[graph.length];
    assertTrue(4 == MaxFlow.maxFlow(graph, new int[graph.length], 0, 2));
  }

  @RepeatedTest(10)
  public void updatesEdges(RepetitionInfo info) {
    Random random = new Random();
    long seed = random.nextLong();
    System.out.println("[ " + info.getCurrentRepetition() + " ] " + seed);
    // seed = 1280261209857013058L;
    random.setSeed(seed);
    final int V = 16 * 16 * 256 + 2;
    final int E = V * 4;

    List<MaxFlow.Edge>[] graphA = MaxFlow.createGraph(V);
    List<MaxFlow.Edge>[] graphB = MaxFlow.createGraph(V);
    List<int[]> toChange = new ArrayList<>();
    for (int i = 0; i < E; i++) {
      int u = random.nextInt(V - 2);
      int v = random.nextInt(V - 2);
      int cap = random.nextInt(42);
      MaxFlow.createEdge(graphA, u, v, cap);
      if (random.nextFloat() < 0.25) {
        int change_cap = random.nextInt(42);
        toChange.add(new int[] {u, graphA[u].size() - 1, change_cap});
        MaxFlow.createEdge(graphB, u, v, change_cap);
      } else {
        MaxFlow.createEdge(graphB, u, v, cap);
      }
    }
    int s = random.nextInt(V - 2);
    int t = random.nextInt(V - 2);
    int preresultA = MaxFlow.maxFlow(graphA, new int[V], s, t);
    System.out.println(preresultA);
    int resultA =
        MaxFlow.changeEdges(graphA, new int[V], s, t, toChange.toArray(new int[3][toChange.size()]), V - 1, V - 2)
            + preresultA;
    int resultB = MaxFlow.maxFlow(graphB, new int[V], s, t);
    assertTrue(MaxFlow.maxFlow(graphA, new int[V], s, t) == 0 && MaxFlow.maxFlow(graphB, new int[V], s, t) == 0);
    assertTrue(resultA == resultB, resultA + " != " + resultB);
  }
}
