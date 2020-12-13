package io.github.jugbot;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import com.google.common.graph.MutableNetwork;

import org.apache.logging.log4j.core.script.Script;
import org.checkerframework.checker.units.qual.s;

import io.github.jugbot.graph.Edge;
import io.github.jugbot.graph.Vertex;
import net.minecraft.server.v1_16_R2.EntityFox.i;

public class Utils {

  /**
   * Validates a graph with flows from source to sink.
   *
   * @param graph
   * @param s source
   * @param t sink
   */
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
    Vertex[] offending =
        debt.entrySet()
            .parallelStream()
            .filter(entry -> entry.getValue() != 0f)
            .map(entry -> entry.getKey())
            .toArray(Vertex[]::new);
    assertEquals(0, offending.length, Arrays.toString(offending));
  }

  /**
   * BFS version of {@link Utils#verifyGraph} to avoid checking disconnected parts of the graph
   *
   * @param graph
   * @param s source
   * @param t sink
   */
  static void verifyGraphConnectedOnly(MutableNetwork<Vertex, Edge> graph, Vertex s, Vertex t) {
    Map<Vertex, Float> debt = new HashMap<>();
    Set<Vertex> visited = new HashSet<>();
    List<Vertex> queue = new ArrayList<>();
    queue.add(s);
    visited.add(s);
    for (int i = 0; i < queue.size(); i++) {
      Vertex u = queue.get(i);
      for (Vertex v : graph.successors(u)) {
        if (!visited.contains(v)) {
          queue.add(v);
          visited.add(v);
        }
        Edge edge = graph.edgeConnectingOrNull(u, v);
        assertTrue(edge.f <= edge.cap, "(" + u + "u) f " + edge.f + " > cap " + edge.cap);
        assertEquals(edge.f, -graph.edgeConnectingOrNull(v, u).f, 0.0f, "Edge flow is not mirrored!");
        debt.put(u, debt.getOrDefault(u, 0f) - edge.f);
      }
    }
    assertTrue(debt.get(s) == -debt.get(t));
    debt.put(s, 0f);
    debt.put(t, 0f);
    Vertex[] offending =
        debt.entrySet()
            .parallelStream()
            .filter(entry -> entry.getValue() != 0f)
            .map(entry -> entry.getKey())
            .toArray(Vertex[]::new);
    assertEquals(0, offending.length, Arrays.toString(offending));
  }

  static void printGraph(MutableNetwork<Vertex, Edge> graph) {
    System.out.println(graph);
  }
}
