package io.github.jugbot;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import io.github.jugbot.util.MaxFlow;

public class MaxFlowTest {
  
  @Test
  public void returnsMaxFlow() {
    List<MaxFlow.Edge>[] graph = MaxFlow.createGraph(3);
    MaxFlow.addEdge(graph, 0, 1, 3);
    MaxFlow.addEdge(graph, 0, 2, 2);
    MaxFlow.addEdge(graph, 1, 2, 2);
    // int[] dist = new int[graph.length]; 
    assertTrue(4 == MaxFlow.maxFlow(graph, new int[graph.length], 0, 2));
  }
  
}