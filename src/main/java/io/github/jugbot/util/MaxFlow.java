package io.github.jugbot.util;

import java.io.Serializable;
import java.util.*;

import io.github.jugbot.IntegrityData;

/**
 * Ground-up change: - Expect "empty" vertices when changing edges - Fast replacement of key edges (i.e. labels)
 * Optimising: - Change index values to shorts (they are good enough for minecraft chunks) - Possibly ensmallen (yes,
 * word) Object ta raw type like char[] - Basically one object for all edges at a node... :/ - Reuse residual edges for
 * two-way connections (most) Future - Support future graph bridging??
 *
 * <p>{@link Edge}
 */
public class MaxFlow {

  public static class Edge implements Serializable {
    public int t;
    public int rev;
    public int cap;
    public int f;

    public Edge(int t, int rev, int cap) {
      this.t = t;
      this.rev = rev;
      this.cap = cap;
    }
  }

  public static List<Edge>[] createGraph(int nodes) {
    List<Edge>[] graph = new List[nodes];
    for (int i = 0; i < nodes; i++)
      graph[i] = new ArrayList<>(Collections.nCopies(IntegrityData.values().length, null));
    return graph;
  }

  public static Edge createEdge(List<Edge>[] graph, int u, int v, int cap) {
    return createEdge(graph, u, v, cap, null);
  }

  public static Edge createEdge(List<Edge>[] graph, int u, int v, int cap, IntegrityData tag) {
    Edge edge;
    if (tag == null) {
      edge = new Edge(v, graph[v].size(), cap);
      graph[u].add(edge);
      graph[v].add(new Edge(u, graph[u].size() - 1, 0));
    } else if (tag == IntegrityData.MASS) {
      edge = new Edge(v, graph[v].size(), cap);
      graph[u].set(tag.ordinal(), edge);
      graph[v].add(new Edge(u, tag.ordinal(), 0));
    } else {
      edge = new Edge(v, tag.opposite().ordinal(), cap);
      graph[u].set(tag.ordinal(), edge);
      if (graph[v].get(tag.opposite().ordinal()) == null) {
        graph[v].set(tag.opposite().ordinal(), new Edge(u, tag.ordinal(), 0));
      }
    }
    return edge;
  }

  private static Edge deleteEdge(List<Edge>[] graph, int u, int e) {
    if (e < IntegrityData.values().length) {
      // do not free reserved slot
      return graph[u].set(e, null);
    } else {
      // remove from list
      int eLast = graph[u].size() - 1;
      if (e < eLast) {
        // shorten array
        Edge edge = graph[u].set(e, graph[u].get(eLast));
        Edge fixme = graph[u].remove(eLast);
        graph[fixme.t].get(fixme.rev).rev = e;
        return edge;
      } else {
        // remove last
        return graph[u].remove(e);
      }
    }
  }

  private static void deleteEdgePair(List<Edge>[] graph, int u, int e) {
    if (graph[u].get(e) == null) return;
    Edge edge = deleteEdge(graph, u, e);
    deleteEdge(graph, edge.t, edge.rev);
  }

  private static void deleteEdgePairs(List<Edge>[] graph, int u) {
    for (int e = graph[u].size() - 1; e >= 0; e = Math.min(e - 1, graph[u].size() - 1)) {
      deleteEdgePair(graph, u, e);
    }
  }

  private static void deleteVertexEdges(List<Edge>[] graph, Iterable<Integer> vertices) {
    for (int u : vertices) {
      // deleteEdgePairs(graph, u);
    }
  }

  public static void pruneEdges(List<Edge>[] graph, int[] dist, int src, int dest, List<int[]> toChange) {
    // remove unused edge pairs for saving memory
  }

  /**
   * Modifies edge weights on existing edges. https://cstheory.stackexchange.com/a/10186
   *
   * @param toChange The vertex index, edge index, and new edge capacity.
   * @return The change in max flow.
   */
  public static int changeEdges(
      List<Edge>[] graph, int[] dist, int s, int t, Iterable<int[]> toChange, int temp_s, int temp_t) {
    deleteEdgePairs(graph, temp_s);
    deleteEdgePairs(graph, temp_t);
    int max_flow = 0;
    for (int[] changeAt : toChange) {
      int u = changeAt[0];
      int e = changeAt[1];
      int cap = changeAt[2];
      assert e < graph[u].size() : "Edge does not exist!";
      Edge existing = graph[u].get(e);
      assert existing != null : "Edge does not exist!";
      // Check if flow should be reduced
      if (existing.f > cap) {
        int df = existing.f - cap;
        existing.f = cap;
        graph[existing.t].get(existing.rev).f = -cap;
        // Create temp edge to rebalance edges later
        createEdge(graph, existing.t, temp_t, df);
        createEdge(graph, temp_s, u, df);
        // potential max flow if all reductions in flow are satisfied later
        max_flow += df;
      }
      existing.cap = cap;
    }
    // If all flow reductions are satisfied, return
    int df = max_flow - MaxFlow.maxFlow(graph, dist, temp_s, temp_t);
    if (df == 0) {
      deleteEdgePairs(graph, temp_s);
      deleteEdgePairs(graph, temp_t);
      return MaxFlow.maxFlow(graph, dist, s, t);
    }
    // Else reduce flow on entire graph
    createEdge(graph, s, t, Integer.MAX_VALUE);
    int final_flow = MaxFlow.maxFlow(graph, dist, temp_s, temp_t);
    assert final_flow == df : ("final_flow: " + final_flow + " should be " + df);
    deleteEdgePair(graph, s, graph[s].size() - 1);
    // Probably harmless but delete edges anyways
    deleteEdgePairs(graph, temp_s);
    deleteEdgePairs(graph, temp_t);
    // flow should be good but level should be set again for consistency
    dinicBfs(graph, s, t, dist);
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
        if (e == null) continue;
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
      if (e == null) continue;
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
    assert src != dest : "Source vertex cannot be the same as the destination!";
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
      if (e == null) continue;
      int u = e.t;
      int level = dist[u];
      if (level > 0) {
        result.add(u);
      }
    }
    return result;
  }
}
