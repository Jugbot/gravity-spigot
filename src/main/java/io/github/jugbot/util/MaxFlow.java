package io.github.jugbot.util;

import java.util.*;

public class MaxFlow {

  public static class Edge {
    int t, rev, cap, f;

    public Edge(int t, int rev, int cap) {
      this.t = t;
      this.rev = rev;
      this.cap = cap;
    }
  }

  public static List<Edge>[] createGraph(int nodes) {
    List<Edge>[] graph = new List[nodes];
    for (int i = 0; i < nodes; i++) graph[i] = new ArrayList<>();
    return graph;
  }

  public static void addEdge(List<Edge>[] graph, int u, int v, int cap) {
    graph[u].add(new Edge(v, graph[v].size(), cap));
    graph[v].add(new Edge(u, graph[u].size() - 1, 0));
  }

  // https://cs.stackexchange.com/questions/86801/how-to-find-max-flow-in-a-graph-after-decrementing-an-edge-capacity
  public static void reduceEdge(List<Edge>[] graph, int s, int t, int u, int e, int delta) {}

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
    // int[] dist = new int[graph.length];
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

  public static List<Integer> getOffendingVertices(
      List<Edge>[] graph, int[] dist, int src, int dest) {
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