package io.github.jugbot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.google.common.graph.EndpointPair;
import com.google.common.graph.MutableNetwork;
import com.google.common.graph.NetworkBuilder;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;
import org.junit.jupiter.api.Test;

import io.github.jugbot.graph.Edge;
import io.github.jugbot.graph.MaxFlow;
import io.github.jugbot.graph.Vertex;

public class MaxFlowTest {

  private Vertex v(int id) {
    return new Vertex(id + 5);
  }

  @Test
  public void returnsMaxFlow() {
    MutableNetwork<Vertex, Edge> graph = NetworkBuilder.directed().build();
    MaxFlow.createEdge(graph, v(0), v(1), 3);
    MaxFlow.createEdge(graph, v(0), v(2), 2);
    MaxFlow.createEdge(graph, v(1), v(2), 4);
    Map<Vertex, Integer> dists = new HashMap<>();

    assertEquals(5, MaxFlow.maxFlow(graph, dists, v(0), v(2)));
  }

  @Test
  public void returnsMaxFlowDisconnected() {
    MutableNetwork<Vertex, Edge> graph = NetworkBuilder.directed().build();
    MaxFlow.createEdge(graph, v(0), v(1), 3);
    MaxFlow.createEdge(graph, v(2), v(0), 2);
    MaxFlow.createEdge(graph, v(1), v(0), 2);
    Map<Vertex, Integer> dists = new HashMap<>();
    assertEquals(0, MaxFlow.maxFlow(graph, dists, v(0), v(2)));
  }

  @Test
  public void returnsMaxFlowAfterIncrementUpdate() {
    MutableNetwork<Vertex, Edge> graph = NetworkBuilder.directed().build();
    MaxFlow.createEdge(graph, v(0), v(1), 3);
    MaxFlow.createEdge(graph, v(0), v(2), 2);
    MaxFlow.createEdge(graph, v(1), v(2), 2);
    Map<Vertex, Integer> dists = new HashMap<>();

    float flow = MaxFlow.maxFlow(graph, dists, v(0), v(2));
    assertEquals(4, flow);
    Map<EndpointPair<Vertex>, Float> toChange = new HashMap<>();
    toChange.put(EndpointPair.ordered(v(1), v(2)), 100f);
    flow += MaxFlow.changeEdges(graph, dists, v(0), v(2), toChange);
    assertEquals(5, flow);
  }

  @Test
  public void returnsMaxFlowAfterDecrementUpdate() {
    MutableNetwork<Vertex, Edge> graph = NetworkBuilder.directed().build();
    MaxFlow.createEdge(graph, v(0), v(1), 3);
    MaxFlow.createEdge(graph, v(0), v(2), 2);
    MaxFlow.createEdge(graph, v(1), v(2), 2);
    Map<Vertex, Integer> dists = new HashMap<>();

    float flow = MaxFlow.maxFlow(graph, dists, v(0), v(2));
    assertEquals(4, flow);
    Map<EndpointPair<Vertex>, Float> toChange = new HashMap<>();
    toChange.put(EndpointPair.ordered(v(1), v(2)), 1f);
    flow += MaxFlow.changeEdges(graph, dists, v(0), v(2), toChange);
    assertEquals(3, flow);
  }

  @Test
  public void returnsMaxFlowAfterUpdateEdgeCase01() {
    MutableNetwork<Vertex, Edge> graph = NetworkBuilder.directed().build();
    MaxFlow.createEdge(graph, v(0), v(1), 1);
    MaxFlow.createEdge(graph, v(1), v(0), 12);
    MaxFlow.createEdge(graph, v(0), v(1), 7);
    Map<Vertex, Integer> dists = new HashMap<>();

    float flow = MaxFlow.maxFlow(graph, dists, v(0), v(1));
    assertEquals(7, flow);
    Map<EndpointPair<Vertex>, Float> toChange = new HashMap<>();
    toChange.put(EndpointPair.ordered(v(0), v(1)), 33f);
    flow += MaxFlow.changeEdges(graph, dists, v(0), v(1), toChange);
    assertEquals(33, flow);
  }

  @Test
  public void returnsMaxFlowAfterUpdateEdgeCase02() {
    MutableNetwork<Vertex, Edge> graph = NetworkBuilder.directed().build();
    MaxFlow.createEdge(graph, v(0), v(1), 33);
    MaxFlow.createEdge(graph, v(1), v(0), 12);
    MaxFlow.createEdge(graph, v(0), v(1), 7);
    Map<Vertex, Integer> dists = new HashMap<>();

    float flow = MaxFlow.maxFlow(graph, dists, v(0), v(1));
    assertEquals(7, flow);
  }

  private static <T extends Enum<?>> T randomEnum(Random random, Class<T> clazz) {
    int x = random.nextInt(clazz.getEnumConstants().length);
    return clazz.getEnumConstants()[x];
  }

  // @RepeatedTest(10)
  // public void reservedEdges(RepetitionInfo info) {
  //   Random random = new Random();
  //   long seed = random.nextLong();
  //   // seed = -8405525021095806718L;
  //   System.out.println("[ " + info.getCurrentRepetition() + " ] " + seed);
  //   random.setSeed(seed);
  //   final int MODIFIER = 16 * 256;
  //   final int V = 16 * 16 * 256 / MODIFIER + 2;
  //   final int E = V * 4;

  //   DefaultList<MaxFlow.Edge> graphA = new DefaultList<>(V);
  //   DefaultList<MaxFlow.Edge> graphB = new DefaultList<>(V);

  //   for (int i = 0; i < E; i++) {
  //     int u = random.nextInt(V - 2);
  //     int v = random.nextInt(V - 2);
  //     int cap = random.nextInt(42);
  //     IntegrityData state = randomEnum(random, IntegrityData.class);
  //     if (random.nextFloat() < 0.7
  //         && graphA.get(u).get(state.ordinal()) == null
  //         && state != IntegrityData.MASS
  //         // Implementation assumes cardinal edges pair with one opposite edge (not multiple)
  //         && graphA.get(v).get(state.opposite().ordinal()) == null) {
  //       MaxFlow.createEdge(graphA, u, v, cap, state);
  //     } else {
  //       MaxFlow.createEdge(graphA, u, v, cap);
  //     }
  //     MaxFlow.createEdge(graphB, u, v, cap);
  //   }
  //   int[] distA = new int[V];
  //   int[] distB = new int[V];
  //   int s = random.nextInt(V - 1 - 2);
  //   int t = s + 1;
  //   System.out.println(s + "s " + t + "t");

  //   int resultA = MaxFlow.maxFlow(graphA, distA, s, t);
  //   int resultB = MaxFlow.maxFlow(graphB, distB, s, t);
  //   System.out.println("Graph A (MODIFIED)");
  //   printGraph(graphA);
  //   System.out.println("Graph B (NEW)");
  //   printGraph(graphB);
  //   verifyGraph(graphA, s, t);
  //   verifyGraph(graphB, s, t);
  //   assertTrue(MaxFlow.maxFlow(graphA, distA, s, t) == 0 && MaxFlow.maxFlow(graphB, distB, s, t) == 0);
  //   assertTrue(resultA == resultB, resultA + " (mod) != " + resultB + " (new)");
  //   int badA = MaxFlow.getOffendingVertices(graphA, distA, s, t).size();
  //   int badB = MaxFlow.getOffendingVertices(graphB, distB, s, t).size();
  //   assertTrue(badA == badB, badA + " (mod) != " + badB + " (new)");
  // }

  // @RepeatedTest(10)
  public void updatesEdgesABTest(RepetitionInfo info) {
    Random random = new Random();
    long seed = random.nextLong();
    // seed = 4661834240350113105L;
    System.out.println("[ " + info.getCurrentRepetition() + " ] " + seed);
    random.setSeed(seed);
    final int V = 16 * 16 * 256 + 2;
    final int E = V * 4;
    assert V > 0 : "Stupid overflows >:(";
    assert E / (V - 1) <= V : "Graph theory forbids this >:("; // Though you want it to be much less

    MutableNetwork<Vertex, Edge> graphA = NetworkBuilder.directed().build();
    MutableNetwork<Vertex, Edge> graphB = NetworkBuilder.directed().build();
    Map<Vertex, Integer> distsA = new HashMap<>();
    Map<Vertex, Integer> distsB = new HashMap<>();
    Vertex u = null, v = null;
    // System.out.println("graph =");
    Map<EndpointPair<Vertex>, Float> toChange = new HashMap<>();
    for (int i = 0; i < E; ) {
      int uInt = random.nextInt(V);
      int vInt = random.nextInt(V);
      u = v(uInt);
      v = v(vInt);

      if (u.equals(v)) continue; // avoid self-loops (Error)
      else if (graphA.nodes().contains(u) && graphA.nodes().contains(v) && graphA.edgeConnectingOrNull(u, v) != null)
        continue; // avoid overwriting edge weight
      else i++;
      int cap = random.nextInt(42);
      // System.out.println("v(" + uInt + "), v(" + vInt + "), " + cap);
      MaxFlow.createEdge(graphA, u, v, cap);
      if (random.nextFloat() < 0.25) {
        float change_cap = random.nextInt(42);
        toChange.put(EndpointPair.ordered(u, v), change_cap);
        // System.out.println("change -> " + change_cap);
        MaxFlow.createEdge(graphB, u, v, change_cap);
      } else {
        MaxFlow.createEdge(graphB, u, v, cap);
      }
    }
    // System.out.println("toChange =");
    // for (Entry<EndpointPair<Vertex>, Float> set : toChange.entrySet()) {
    //   System.out.println("v(" + set.getKey().nodeU() + "), v(" + set.getKey().nodeV() + "), " + set.getValue());
    // }
    Vertex s = u;
    Vertex t = v;

    int resultA = MaxFlow.maxFlow(graphA, distsA, s, t);
    resultA += MaxFlow.changeEdges(graphA, distsA, s, t, toChange);
    int resultB = MaxFlow.maxFlow(graphB, distsB, s, t);
    System.out.println("Graph A (MODIFIED)");
    printGraph(graphA);
    System.out.println("Graph B (NEW)");
    printGraph(graphB);
    verifyGraph(graphA, s, t);
    verifyGraph(graphB, s, t);
    assertTrue(MaxFlow.maxFlow(graphA, distsA, s, t) == 0 && MaxFlow.maxFlow(graphB, distsB, s, t) == 0);
    assertTrue(resultA == resultB, resultA + " (mod) != " + resultB + " (new)");
    int badA = MaxFlow.getOffendingVertices(graphA, distsA, s, t).size();
    int badB = MaxFlow.getOffendingVertices(graphB, distsB, s, t).size();
    assertTrue(badA == badB, badA + " (mod) != " + badB + " (new)");
  }

  @Test
  public void bigMaxFlow() {
    Random random = new Random();
    long seed = System.currentTimeMillis();
    // seed = -8461310760728933339L;
    System.out.println("[ BigFlow ] " + seed);
    random.setSeed(seed);
    final int V = 16 * 16 * 256;
    final int E = V * 6;
    assert V > 0 : "Stupid overflows >:(";
    assert E / (V - 1) <= V : "Graph theory forbids this >:("; // Though you want it to be much less

    MutableNetwork<Vertex, Edge> graphA = NetworkBuilder.directed().build();
    Map<Vertex, Integer> distsA = new HashMap<>();
    Vertex u = null, v = null;
    System.out.println("graph =");
    for (int i = 0; i < E; ) {
      int uInt = random.nextInt(V);
      int vInt = random.nextInt(V);
      u = v(uInt);
      v = v(vInt);

      if (u.equals(v)) continue; // avoid self-loops (Error)
      else i++;
      int cap = random.nextInt(42);
      System.out.println("v(" + uInt + "), v(" + vInt + "), " + cap);
      MaxFlow.createEdge(graphA, u, v, cap);
    }
    Vertex s = u;
    Vertex t = v;

    int resultA = MaxFlow.maxFlow(graphA, distsA, s, t);
    System.out.println("Graph A (MODIFIED)");
    // printGraph(graphA);
    // verifyGraph(graphA, s, t);
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

  private static void verifyGraph(MutableNetwork<Vertex, Edge> graph, Vertex s, Vertex t) {
    // int[] debt = new int[graph.size()];
    // for (int u = 0; u < graph.size(); u++) {
    //   for (Edge edge : graph.get(u)) {
    //     if (edge == null) continue;
    //     assertTrue(edge.f <= edge.cap, "(" + u + "u) f " + edge.f + " > cap " + edge.cap);
    //     assertEquals(edge.f, -graph.get(edge.t).get(edge.rev).f, 0.0f, "Edge flow is not mirrored!");
    //     if (edge.f > 0) {
    //       debt[u] -= edge.f;
    //       debt[edge.t] += edge.f;
    //     }
    //   }
    // }
    // assertTrue(debt[s] == -debt[t]);
    // debt[s] = 0;
    // debt[t] = 0;
    // assertTrue(Arrays.equals(debt, new int[graph.size()]));
  }

  private static void printGraph(MutableNetwork<Vertex, Edge> graph) {
    // for (Edge edge : graph.edges()) {
    //   EndpointPair<Vertex> uv = graph.incidentNodes(edge);
    //   System.out.println("v(");
    // }
  }
}
