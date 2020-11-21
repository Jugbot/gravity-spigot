package io.github.jugbot.graph;

import java.util.*;
import java.util.Map.Entry;

import javax.lang.model.type.ArrayType;
import javax.swing.text.html.parser.Entity;

import com.google.common.graph.ElementOrder;
import com.google.common.graph.EndpointPair;
import com.google.common.graph.MutableNetwork;

import org.bukkit.block.data.type.EndPortalFrame;

import com.google.common.graph.MutableNetwork;

import io.github.jugbot.App;
import io.github.jugbot.IntegrityData;

public class MaxFlow {

  private MaxFlow() {}

  public static void createEdge(MutableNetwork<Vertex, Edge> graph, Vertex u, Vertex v, float cap) {
    Edge existing = null;
    if (graph.nodes().contains(u) && graph.nodes().contains(v)) existing = graph.edgeConnectingOrNull(u, v);
    if (existing != null) {
      existing.cap = cap;
    } else {
      graph.addEdge(u, v, new Edge(cap));
    }
    // Add back edge for maxflow
    if (graph.edgesConnecting(v, u).isEmpty()) {
      graph.addEdge(v, u, new Edge(0));
    }
  }

  private static void createEdgeOrIncrement(MutableNetwork<Vertex, Edge> graph, Vertex u, Vertex v, float cap) {
    Edge existing = null;
    if (graph.nodes().contains(u) && graph.nodes().contains(v)) existing = graph.edgeConnectingOrNull(u, v);
    if (existing != null) {
      existing.cap += cap;
    } else {
      graph.addEdge(u, v, new Edge(cap));
    }
    // Add back edge for maxflow
    if (graph.edgesConnecting(v, u).isEmpty()) {
      graph.addEdge(v, u, new Edge(0));
    }
  }

  public static void pruneEdges(MutableNetwork<Vertex, Edge> graph) {
    // remove unused edge pairs for saving memory
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
    // deleteEdgePairs(graph, temp_s);
    // deleteEdgePairs(graph, temp_t);
    // int max_flow = 0;
    // for (int[] changeAt : toChange) {
    //   int u = changeAt[0];
    //   int e = changeAt[1];
    //   int cap = changeAt[2];
    //   assert e < graph.get(u).size() : "Edge does not exist!";
    //   Edge existing = graph.get(u).get(e);
    //   assert existing != null : "Edge does not exist!";
    //   // Check if flow should be reduced
    //   if (existing.f > cap) {
    //     float df = existing.f - cap;
    //     existing.f = cap;
    //     graph.get(existing.t).get(existing.rev).f = -cap;
    //     // Create temp edge to rebalance edges later
    //     createEdge(graph, existing.t, temp_t, df);
    //     createEdge(graph, temp_s, u, df);
    //     // potential max flow if all reductions in flow are satisfied later
    //     max_flow += df;
    //   }
    //   existing.cap = cap;
    // }
    // // If all flow reductions are satisfied, return
    // int df = max_flow - MaxFlow.maxFlow(graph, dist, temp_s, temp_t);
    // if (df == 0) {
    //   deleteEdgePairs(graph, temp_s);
    //   deleteEdgePairs(graph, temp_t);
    //   return MaxFlow.maxFlow(graph, dist, s, t);
    // }
    // // Else reduce flow on entire graph
    // createEdge(graph, s, t, Integer.MAX_VALUE);
    // int final_flow = MaxFlow.maxFlow(graph, dist, temp_s, temp_t);
    // assert final_flow == df : ("final_flow: " + final_flow + " should be " + df);
    // deleteEdgePair(graph, s, graph.get(s).size() - 1);
    // // Probably harmless but delete edges anyways
    // deleteEdgePairs(graph, temp_s);
    // deleteEdgePairs(graph, temp_t);
    // // flow should be good but level should be set again for consistency
    // dinicBfs(graph, s, t, dist);
    // return -final_flow;
    Vertex temp_s = new Vertex(null, -1);
    Vertex temp_t = new Vertex(null, -2);
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
      // deleteEdgePairs(graph, temp_s);
      // deleteEdgePairs(graph, temp_t);
      graph.removeNode(temp_s);
      graph.removeNode(temp_t);
      return MaxFlow.maxFlow(graph, dists, s, t);
    }
    // Else reduce flow on entire graph
    createEdge(graph, s, t, Integer.MAX_VALUE);
    int final_flow = MaxFlow.maxFlow(graph, dists, temp_s, temp_t);
    assert final_flow == df : ("final_flow: " + final_flow + " should be " + df);
    graph.removeEdge(graph.edgeConnectingOrNull(s, t));
    // Probably harmless but delete edges anyways
    // deleteEdgePairs(graph, temp_s);
    // deleteEdgePairs(graph, temp_t);
    graph.removeNode(temp_s);
    graph.removeNode(temp_t);
    // flow should be good but level should be set again for consistency
    dinicBfs(graph, dists, s, t);
    return -final_flow;
  }

  private static boolean dinicBfs(
      MutableNetwork<Vertex, Edge> graph, Map<Vertex, Integer> dists, Vertex src, Vertex dest) {
    // Arrays.fill(dist, -1);
    // dist[src] = 0;
    // int[] Q = new int[graph.size()];
    // int sizeQ = 0;
    // Q[sizeQ++] = src;
    // for (int i = 0; i < sizeQ; i++) {
    //   int u = Q[i];
    //   for (Edge e : graph.get(u)) {
    //     if (e == null) continue;
    //     if (dist[e.t] < 0 && e.f < e.cap) {
    //       dist[e.t] = dist[u] + 1;
    //       Q[sizeQ++] = e.t;
    //     }
    //   }
    // }
    // return dist[dest] > -1;

    // dists.clear();
    for (Vertex v : graph.nodes()) {
      dists.put(v, -1);
    }
    assert (dists.containsKey(src) && dists.get(src) == -1 && dists.containsKey(dest) && dists.get(dest) == -1)
        : "Vertex is not in the graph (get the real one first!)";
    dists.put(src, 0);
    Vertex[] Q = new Vertex[graph.nodes().size()];
    int sizeQ = 0;
    Q[sizeQ++] = src;
    for (int i = 0; i < sizeQ; i++) {
      Vertex u = Q[i];
      for (Vertex v : graph.successors(u)) {
        Edge e = graph.edgeConnectingOrNull(u, v);
        if (dists.get(v) < 0 && e.f < e.cap) {
          dists.put(v, dists.get(u) + 1);
          Q[sizeQ++] = v;
        }
      }
    }
    return dists.get(dest) > -1;
  }

  private static float dinicDfs(
      MutableNetwork<Vertex, Edge> graph,
      Map<Vertex, Integer> dists,
      Map<Vertex, Set<Edge>> ptr,
      Vertex dest,
      Vertex src,
      float f) {
    // if (u == dest) return f;
    // for (; ptr[u] < graph.get(u).size(); ++ptr[u]) {
    //   Edge e = graph.get(u).get(ptr[u]);
    //   if (e == null) continue;
    //   if (dist[e.t] == dist[u] + 1 && e.f < e.cap) {
    //     float df = dinicDfs(graph, ptr, dist, dest, e.t, Math.min(f, e.cap - e.f));
    //     if (df > 0) {
    //       e.f += df;
    //       graph.get(e.t).get(e.rev).f -= df;
    //       return df;
    //     }
    //   }
    // }
    // return 0;
    if (src.equals(dest)) return f;
    if (!ptr.containsKey(src)) {
      ptr.put(src, new HashSet<>(graph.outEdges(src)));
    }
    while (!ptr.get(src).isEmpty()) {
      Edge e = ptr.get(src).iterator().next();
      ptr.get(src).remove(e);
      EndpointPair<Vertex> uv = graph.incidentNodes(e);
      if (dists.get(uv.nodeV()) == dists.get(uv.nodeU()) + 1 && e.f < e.cap) {
        float df = dinicDfs(graph, dists, ptr, dest, uv.nodeV(), Math.min(f, e.cap - e.f));
        if (df > 0) {
          e.f += df;
          EndpointPair<Vertex> vu = EndpointPair.ordered(uv.nodeV(), uv.nodeU());
          Optional<Edge> edgeOptional = graph.edgeConnecting(vu);
          if (edgeOptional.isPresent()) {
            edgeOptional.get().f -= df;
          } else {
            // Reverse edge does not exist
            App.Instance().getLogger().warning("Reverse edge not found!");
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

  public static int maxFlow(MutableNetwork<Vertex, Edge> graph, Map<Vertex, Integer> dists, Vertex src, Vertex dest) {
    // assert src != dest : "Source vertex cannot be the same as the destination!";
    // int flow = 0;
    // while (dinicBfs(graph, src, dest, dist)) {
    //   List<Integer> loc = getOffendingVertices(graph, dist, src, dest); // TODO: remove
    //   int[] ptr = new int[graph.size()]; // keeps track of visited edges per vertex
    //   while (true) {
    //     float df = dinicDfs(graph, ptr, dist, dest, src, Float.POSITIVE_INFINITY);
    //     if (df == 0) break;
    //     flow += df;
    //   }
    // }
    // return flow;

    // assert graph.edgeOrder().type() == ElementOrder.Type.INSERTION : "Edge order must be consistent for dfs";
    assert src != dest : "Source vertex cannot be the same as the destination!";
    // shortcut
    if (!graph.nodes().contains(src) || !graph.nodes().contains(dest)) return 0;

    int flow = 0;
    while (dinicBfs(graph, dists, src, dest)) {
      // List<Vertex> loc = getOffendingVertices(graph, src, dest); // TODO: remove
      Map<Vertex, Set<Edge>> ptr = new HashMap<>(graph.nodes().size()); // keeps track of visited edges per vertex
      while (true) {
        float df = dinicDfs(graph, dists, ptr, dest, src, Float.POSITIVE_INFINITY);
        if (df == 0) break;
        flow += df;
      }
    }
    return flow;
  }

  public static List<Vertex> getOffendingVertices(
      MutableNetwork<Vertex, Edge> graph, Map<Vertex, Integer> dists, Vertex src, Vertex dest) {
    // TODO: also flag chunk edge violations
    dinicBfs(graph, dists, src, dest);
    List<Vertex> result = new ArrayList<>();
    for (Edge e : graph.outEdges(src)) {
      Vertex v = graph.incidentNodes(e).nodeV();
      if (dists.get(v) > 0 && e.cap > 0) {
        result.add(v);
      }
    }
    return result;
  }
}
