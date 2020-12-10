package io.github.jugbot;

import java.util.HashMap;
import java.util.Map;

import com.google.common.graph.MutableNetwork;

import io.github.jugbot.graph.Edge;
import io.github.jugbot.graph.Vertex;

public class Asserts {
  
	public static void verifyGraph(MutableNetwork<Vertex, Edge> graph, Vertex s, Vertex t) {
    Map<Vertex, Float> debt = new HashMap<>();
    for (Vertex u : graph.nodes()) {
      for (Vertex v : graph.successors(u)) {
        Edge edge = graph.edgeConnectingOrNull(u, v);
        assert edge.f <= edge.cap : "(" + u + "u) f " + edge.f + " > cap " + edge.cap;
        assert edge.f == -graph.edgeConnectingOrNull(v, u).f : "Edge flow is not mirrored!";
        debt.put(u, debt.getOrDefault(u, 0f) - edge.f);
      }
    }
    assert (debt.get(s) == -debt.get(t));
    debt.put(s, 0f);
    debt.put(t, 0f);
    assert (debt.values().parallelStream().allMatch(fl -> fl == 0f));
  }
}
