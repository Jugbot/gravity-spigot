package io.github.jugbot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;

import com.google.common.graph.MutableNetwork;

import io.github.jugbot.graph.Edge;
import io.github.jugbot.graph.Vertex;

public class Utils {

	static void verifyGraph(MutableNetwork<Vertex, Edge> graph, Vertex s, Vertex t) {
    Map<Vertex, Float> debt = new HashMap<>();
    for (Vertex u : graph.nodes()) {
      for (Vertex v : graph.successors(u)) {
        Edge edge = graph.edgeConnectingOrNull(u, v);
        assertTrue(edge.f <= edge.cap, "(" + u + "u) f " + edge.f + " > cap " + edge.cap);
        assertEquals(edge.f, -graph.edgeConnectingOrNull(v, u).f, 0.0f, "Edge flow is not mirrored!");
        debt.put(u, debt.getOrDefault(u, 0f) - edge.f);
      }
    }
    assertTrue(debt.get(s) == -debt.get(t));
    debt.put(s, 0f);
    debt.put(t, 0f);
    assertTrue(debt.values().parallelStream().allMatch(fl -> fl == 0f));
  }

	static void printGraph(MutableNetwork<Vertex, Edge> graph) {
    System.out.println(graph);  
  }
  
}
