package io.github.jugbot.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.graph.EndpointPair;
import com.google.common.graph.MutableNetwork;

public class MaxFlow {

  private MaxFlow() {}

  public static void createEdge(MutableNetwork<Vertex, Edge> graph, Vertex u, Vertex v, float cap) {
    graph.addNode(u);
    graph.addNode(v);
    Optional<Edge> existing = graph.edgeConnecting(u, v);
    if (existing.isPresent()) {
      existing.get().cap = cap;
    } else {
      graph.addEdge(u, v, new Edge(cap));
    }
    // Add back edge for maxflow
    if (graph.edgesConnecting(v, u).isEmpty()) {
      graph.addEdge(v, u, new Edge(0));
    }
  }

  private static void createEdgeOrIncrement(MutableNetwork<Vertex, Edge> graph, Vertex u, Vertex v, float cap) {
    graph.addNode(u);
    graph.addNode(v);
    Optional<Edge> existing = graph.edgeConnecting(u, v);
    if (existing.isPresent()) {
      existing.get().cap += cap;
    } else {
      graph.addEdge(u, v, new Edge(cap));
    }
    // Add back edge for maxflow
    if (graph.edgesConnecting(v, u).isEmpty()) {
      graph.addEdge(v, u, new Edge(0));
    }
  }

  public static void pruneEdges(MutableNetwork<Vertex, Edge> graph) {
    // TODO: remove unused edge pairs for saving memory
  }

  /**
   * Modifies edge weights on existing edges. https://cstheory.stackexchange.com/a/10186
   *
   * @param toChange The vertex index, edge index, and new edge capacity.
   * @return The change in max flow.
   */
  public static int changeEdges(
      MutableNetwork<Vertex, Edge> graph,
      Map<Vertex, Integer> dists,
      Vertex s,
      Vertex t,
      Map<EndpointPair<Vertex>, Float> toChange) {
    // makes sure flow is already at maximum
    MaxFlow.maxFlow(graph, dists, s, t);
    Vertex temp_s = new Vertex(ReservedID.TEMP_SOURCE);
    Vertex temp_t = new Vertex(ReservedID.TEMP_DEST);
    assert !graph.nodes().contains(temp_s) && !graph.nodes().contains(temp_t) : "Temp nodes should not exist yet!";
    dists.put(temp_s, -1);
    dists.put(temp_t, -1);
    int max_flow = 0;
    for (Entry<EndpointPair<Vertex>, Float> changeAt : toChange.entrySet()) {
      EndpointPair<Vertex> uv = changeAt.getKey();
      float cap = changeAt.getValue();
      Edge existing = graph.edgeConnectingOrNull(uv);
      assert existing != null : "Edge does not exist!";
      // Check if flow should be reduced
      if (existing.f > cap) {
        float df = existing.f - cap;
        existing.f = cap;
        Edge rev = graph.edgeConnectingOrNull(uv.nodeV(), uv.nodeU());
        assert rev != null : "Reverse edge does not exist!";
        rev.f = -cap;
        // Create temp edge to rebalance edges later
        createEdgeOrIncrement(graph, uv.nodeV(), temp_t, df);
        createEdgeOrIncrement(graph, temp_s, uv.nodeU(), df);
        // potential max flow if all reductions in flow are satisfied later
        max_flow += df;
      }
      existing.cap = cap;
    }
    // If all flow reductions are satisfied, return
    int df = max_flow - MaxFlow.maxFlow(graph, dists, temp_s, temp_t);
    if (df == 0) {
      graph.removeNode(temp_s);
      graph.removeNode(temp_t);
      return MaxFlow.maxFlow(graph, dists, s, t);
    }
    // Else reduce flow on entire graph
    createEdge(graph, s, t, Integer.MAX_VALUE);
    int final_flow = MaxFlow.maxFlow(graph, dists, temp_s, temp_t);
    assert final_flow == df : ("final_flow: " + final_flow + " should be " + df);
    graph.removeEdge(graph.edgeConnectingOrNull(s, t));
    graph.removeNode(temp_s);
    graph.removeNode(temp_t);
    // flow should be good but level should be set again for consistency
    dinicBfs(graph, dists, s, t);
    return -final_flow;
  }

  private static boolean dinicBfs(
      MutableNetwork<Vertex, Edge> graph, Map<Vertex, Integer> dists, Vertex src, Vertex dest) {
    dists.clear();
    dists.put(src, 0);
    Vertex[] Q = new Vertex[graph.nodes().size()];
    int sizeQ = 0;
    Q[sizeQ++] = src;
    for (int i = 0; i < sizeQ; i++) {
      Vertex u = Q[i];
      for (Vertex v : graph.successors(u)) {
        Edge e = graph.edgeConnectingOrNull(u, v);
        if (dists.getOrDefault(v, -1) < 0 && e.f < e.cap) {
          dists.put(v, dists.getOrDefault(u, -1) + 1);
          Q[sizeQ++] = v;
        }
      }
    }
    return dists.getOrDefault(dest, -1) > -1;
  }

  private static float dinicDfs(
      MutableNetwork<Vertex, Edge> graph,
      Map<Vertex, Integer> dists,
      Map<Vertex, Set<Edge>> ptr,
      Vertex dest,
      Vertex src,
      float f) {
    if (src.equals(dest)) return f;
    if (!ptr.containsKey(src)) {
      ptr.put(src, new HashSet<>(graph.outEdges(src)));
    }
    Iterator<Edge> unvisitedSet = ptr.get(src).iterator();
    while (unvisitedSet.hasNext()) {
      Edge e = unvisitedSet.next();
      unvisitedSet.remove();
      EndpointPair<Vertex> uv = graph.incidentNodes(e);
      if (dists.getOrDefault(uv.nodeV(), -1) == dists.getOrDefault(uv.nodeU(), -1) + 1 && e.f < e.cap) {
        float df = dinicDfs(graph, dists, ptr, dest, uv.nodeV(), Math.min(f, e.cap - e.f));
        if (df > 0) {
          e.f += df;
          EndpointPair<Vertex> vu = EndpointPair.ordered(uv.nodeV(), uv.nodeU());
          Optional<Edge> edgeOptional = graph.edgeConnecting(vu);
          if (edgeOptional.isPresent()) {
            edgeOptional.get().f -= df;
          } else {
            // Reverse edge does not exist
            // Attempt recovery
            Edge edge = new Edge(0);
            edge.f -= df;
            graph.addEdge(vu, edge);
          }
          return df;
        }
      }
    }
    return 0;
  }

  @VisibleForTesting
  public static int maxFlow(MutableNetwork<Vertex, Edge> graph, Map<Vertex, Integer> dists, Vertex src, Vertex dest) {
    assert src != dest : "Source vertex cannot be the same as the destination!";
    // shortcut
    if (!graph.nodes().contains(src) || !graph.nodes().contains(dest)) return 0;

    int flow = 0;
    while (dinicBfs(graph, dists, src, dest)) {
      // keeps track of visited edges per vertex
      Map<Vertex, Set<Edge>> ptr = new HashMap<>(graph.nodes().size());
      while (true) {
        float df = dinicDfs(graph, dists, ptr, dest, src, Float.POSITIVE_INFINITY);
        if (df == 0) break;
        flow += df;
        // System.out.println(flow / (double) 65536); // tracks progress (performance debugging)
      }
    }
    return flow;
  }

  public static List<Vertex> getOffendingVertices(
      MutableNetwork<Vertex, Edge> graph, Map<Vertex, Integer> dists, Vertex src, Vertex dest) {
    // TODO: also flag chunk edge violations
    // if (dists.getOrDefault(dest, -1) == -1)
    dinicBfs(graph, dists, src, dest);
    List<Vertex> result = new ArrayList<>();
    for (Edge e : graph.outEdges(src)) {
      Vertex v = graph.incidentNodes(e).nodeV();
      if (dists.getOrDefault(v, -1) > 0 && e.cap > 0) {
        result.add(v);
      }
    }
    return result;
  }
}
