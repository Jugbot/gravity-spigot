package io.github.jugbot.util;

import java.util.*;

import io.github.jugbot.IntegrityData;

public class MaxFlow {

  public static class Edge {
    int t, rev, cap, f;
    IntegrityData tag;

    public Edge(int t, int rev, int cap, IntegrityData tag) {
      this.t = t;
      this.rev = rev;
      this.cap = cap;
      this.tag = tag;
    }
  }

  public static List<Edge>[] createGraph(int nodes) {
    List<Edge>[] graph = new List[nodes];
    for (int i = 0; i < nodes; i++) graph[i] = new ArrayList<>();
    return graph;
  }

  public static void createEdge(List<Edge>[] graph, int u, int v, int cap) {
    createEdge(graph, u, v, cap, null);
  }

  public static void createEdge(List<Edge>[] graph, int u, int v, int cap, IntegrityData tag) {
    graph[u].add(new Edge(v, graph[v].size(), cap, tag));
    graph[v].add(new Edge(u, graph[u].size() - 1, 0, null));
  }

  private static void deleteEdge(List<Edge>[] graph, int u, int e) {
    Edge edge = graph[u].remove(e);
    graph[edge.t].remove(edge.rev);
  }

  private static void deleteEdges(List<Edge>[] graph, int u) {
    for (int i = graph[u].size() - 1; i >= 0; i--) {
      if (graph[u].get(i).cap == 0) continue;
      deleteEdge(graph, u, i);
    }
  }

  /**
   * Modifies edge weights on existing edges. https://cstheory.stackexchange.com/a/10186
   *
   * @param toChange The vertex index, edge index, and new edge capacity.
   * @return The change in max flow.
   */
  public static int changeEdges(
      List<Edge>[] graph, int[] dist, int s, int t, int[][] toChange, int temp_s, int temp_t) {
    deleteEdges(graph, temp_s);
    deleteEdges(graph, temp_t);
    int max_flow = 0;
    List<Edge> foLater = new ArrayList<>();
    for (int[] changeAt : toChange) {
      int u = changeAt[0];
      int e = changeAt[1];
      int cap = changeAt[2];
      assert e < graph[u].size() : "Edge does not exist!";
      Edge existing = graph[u].get(e);
      // Check if flow should be reduced
      if (existing.f > cap) {
        int df = existing.f - cap;
        // Create temp edge to rebalance edges later
        createEdge(graph, existing.t, temp_t, df); // was inf
        createEdge(graph, temp_s, u, df);
        // Save edge for if we need to do further fixing of residual graph
        foLater.add(graph[temp_s].get(graph[temp_s].size() - 1));
        // potential max flow if all reductions in flow are satisfied later
        max_flow += df;
      }
      existing.cap = cap;
    }
    // If all flow reductions are satisfied, return
    int df = max_flow - MaxFlow.maxFlow(graph, dist, temp_s, temp_t);
    if (df == 0) return MaxFlow.maxFlow(graph, dist, s, t);
    // Else reduce flow on entire graph
    // NOTE: Might flow too much?
    createEdge(graph, s, t, Integer.MAX_VALUE);
    int final_flow = MaxFlow.maxFlow(graph, dist, temp_s, temp_t);
    assert final_flow == df : ("final_flow: " + final_flow + " should be " + df);
    deleteEdge(graph, s, graph[s].size() - 1);
    // Probably harmless but delete edges anyways
    deleteEdges(graph, temp_s);
    deleteEdges(graph, temp_t);
    return -final_flow;
  }

  static boolean dinicBfs(List<Edge>[] graph, int src, int dest, int[] dist) {
    Arrays.fill(dist, -1);
    dist[src] = 0;
    int[] Q = new int[graph.length];
    int sizeQ = 0;
    Q[sizeQ++] = src;
    for (int i = 0; i < sizeQ; i++) {
      int u = Q[i];
      for (Edge e : graph[u]) {
        if (dist[e.t] < 0 && e.f < e.cap) {
          dist[e.t] = dist[u] + 1;
          Q[sizeQ++] = e.t;
        }
      }
    }
    return dist[dest] >= 0;
  }

  static int dinicDfs(List<Edge>[] graph, int[] ptr, int[] dist, int dest, int u, int f) {
    if (u == dest) return f;
    for (; ptr[u] < graph[u].size(); ++ptr[u]) {
      Edge e = graph[u].get(ptr[u]);
      if (dist[e.t] == dist[u] + 1 && e.f < e.cap) {
        int df = dinicDfs(graph, ptr, dist, dest, e.t, Math.min(f, e.cap - e.f));
        if (df > 0) {
          e.f += df;
          graph[e.t].get(e.rev).f -= df;
          return df;
        }
      }
    }
    return 0;
  }

  public static int maxFlow(List<Edge>[] graph, int[] dist, int src, int dest) {
    int flow = 0;
    while (dinicBfs(graph, src, dest, dist)) {
      int[] ptr = new int[graph.length];
      while (true) {
        int df = dinicDfs(graph, ptr, dist, dest, src, Integer.MAX_VALUE);
        if (df == 0) break;
        flow += df;
      }
    }
    return flow;
  }

  public static List<Integer> getOffendingVertices(List<Edge>[] graph, int[] dist, int src, int dest) {
    List<Integer> result = new ArrayList<>();
    for (Edge e : graph[src]) {
      int u = e.t;
      int level = dist[u];
      if (level > 0 && level != dest) {
        result.add(u);
      }
    }
    return result;
  }
}
