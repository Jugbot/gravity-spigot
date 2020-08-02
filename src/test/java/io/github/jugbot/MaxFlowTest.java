package io.github.jugbot;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

public class MaxFlowTest {
  
  @Test
  public void returnsMaxFlow() {
    List<MaxFlow.Edge>[] graph = MaxFlow.createGraph(3);
    MaxFlow.addEdge(graph, 0, 1, 3);
    MaxFlow.addEdge(graph, 0, 2, 2);
    MaxFlow.addEdge(graph, 1, 2, 2);
    assertTrue(4 == MaxFlow.maxFlow(graph, 0, 2));
  }
  
}