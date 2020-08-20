package io.github.jugbot;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;
import org.junit.jupiter.api.Test;

import io.github.jugbot.util.MaxFlow;

public class MaxFlowTest {

  @Test
  public void reservesIndices() {
    List<MaxFlow.Edge>[] graph = MaxFlow.createGraph(3);
    graph[0].set(3, null);
  }

  @Test
  public void returnsMaxFlow() {
    List<MaxFlow.Edge>[] graph = MaxFlow.createGraph(3);
    MaxFlow.createEdge(graph, 0, 1, 3);
    MaxFlow.createEdge(graph, 0, 2, 2);
    MaxFlow.createEdge(graph, 1, 2, 2);
    // int[] dist = new int[graph.length];
    assertTrue(4 == MaxFlow.maxFlow(graph, new int[graph.length], 0, 2));
  }

  @Test
  public void reservedEdgesSimple() {
    List<MaxFlow.Edge>[] graph = MaxFlow.createGraph(3);
    MaxFlow.createEdge(graph, 0, 1, 3, IntegrityData.MASS);
    MaxFlow.createEdge(graph, 0, 2, 2, IntegrityData.UP);
    MaxFlow.createEdge(graph, 1, 2, 2);
    // int[] dist = new int[graph.length];
    assertTrue(4 == MaxFlow.maxFlow(graph, new int[graph.length], 0, 2));
  }

  private static <T extends Enum<?>> T randomEnum(Random random, Class<T> clazz) {
    int x = random.nextInt(clazz.getEnumConstants().length);
    return clazz.getEnumConstants()[x];
  }

  @RepeatedTest(10)
  public void reservedEdges(RepetitionInfo info) {
    Random random = new Random();
    long seed = random.nextLong();
    System.out.println("[ " + info.getCurrentRepetition() + " ] " + seed);
    // seed = -62000569186895826L;
    random.setSeed(seed);
    final int MODIFIER = 256 * 16;
    final int V = 16 * 16 * 256 / MODIFIER + 2;
    final int E = V * 4;

    List<MaxFlow.Edge>[] graph = MaxFlow.createGraph(V);

    for (int i = 0; i < E; i++) {
      int u = random.nextInt(V - 2);
      int v = random.nextInt(V - 2);
      int cap = random.nextInt(42);
      if (random.nextFloat() < 0.7) {
        MaxFlow.createEdge(graph, u, v, cap, randomEnum(random, IntegrityData.class));
      } else {
        MaxFlow.createEdge(graph, u, v, cap);
      }
    }
    int[] dist = new int[V];
    int s = random.nextInt(V - 1 - 2);
    int t = s + 1;
    System.out.println(s + "s " + t + "t");
    MaxFlow.maxFlow(graph, dist, s, t);
  }

  @RepeatedTest(10)
  public void updatesEdges(RepetitionInfo info) {
    Random random = new Random();
    long seed = random.nextLong();
    System.out.println("[ " + info.getCurrentRepetition() + " ] " + seed);
    // seed = -62000569186895826L;
    random.setSeed(seed);
    final int MODIFIER = 256 * 16;
    final int V = 16 * 16 * 256 / MODIFIER + 2;
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
    int[] distA = new int[V];
    int[] distB = new int[V];
    int s = random.nextInt(V - 1 - 2);
    int t = s + 1;
    System.out.println(s + "s " + t + "t");

    int resultA = MaxFlow.maxFlow(graphA, distA, s, t);
    resultA += MaxFlow.changeEdges(graphA, distA, s, t, toChange, V - 1, V - 2);
    int resultB = MaxFlow.maxFlow(graphB, distB, s, t);
    assertTrue(MaxFlow.maxFlow(graphA, distA, s, t) == 0 && MaxFlow.maxFlow(graphB, distB, s, t) == 0);
    assertTrue(resultA == resultB, resultA + " (mod) != " + resultB + " (new)");
    System.out.println("Graph A (MODIFIED)");
    printGraph(graphA);
    System.out.println("Graph B (NEW)");
    printGraph(graphB);
    // int differences = differences(distA, distB);
    // assertTrue(differences == 0, "Vertex level array is not equivalent! " + differences + " differences.");
    int badA = MaxFlow.getOffendingVertices(graphA, distA, s, t).size();
    int badB = MaxFlow.getOffendingVertices(graphB, distB, s, t).size();
    assertTrue(badA == badB, badA + " (mod) != " + badB + " (new)");
    verifyGraph(graphA, s, t);
    verifyGraph(graphB, s, t);
  }

  private static int differences(int[] a, int[] b) {
    assert a != null && b != null;
    assert a.length == b.length;
    int count = 0;
    for (int i = 0; i < a.length; i++) {
      if (a[i] != b[i]) count++;
    }
    return count;
  }

  private static void verifyGraph(List<MaxFlow.Edge>[] graph, int s, int t) {
    int[] debt = new int[graph.length];
    for (int u = 0; u < graph.length; u++) {
      for (MaxFlow.Edge edge : graph[u]) {
        if (edge == null) continue;
        assertTrue(edge.f <= edge.cap, "(" + u + "u) f " + edge.f + " > cap " + edge.cap);
        assertTrue(edge.f == -graph[edge.t].get(edge.rev).f, "Edge flow is not mirrored!");
        if (edge.f > 0) {
          debt[u] -= edge.f;
          debt[edge.t] += edge.f;
        }
      }
    }
    assertTrue(debt[s] == -debt[t]);
    debt[s] = 0;
    debt[t] = 0;
    assertTrue(Arrays.equals(debt, new int[graph.length]));
  }

  private static void printGraph(List<MaxFlow.Edge>[] graph) {
    for (int u = 0; u < graph.length; u++) {
      System.out.print("NODE " + u + "");
      for (MaxFlow.Edge edge : graph[u]) {
        if (edge == null) continue;
        System.out.print(" " + edge.t + "(" + edge.f + ")");
      }
      System.out.println();
    }
  }

  public void updatesEdgesIntegrityData(RepetitionInfo info) {
    Random random = new Random();
    long seed = random.nextLong();
    System.out.println("[ " + info.getCurrentRepetition() + " ] " + seed);
    // seed = 1280261209857013058L;
    random.setSeed(seed);
  }
}
