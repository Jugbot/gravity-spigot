package io.github.jugbot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.common.graph.MutableNetwork;
import com.google.common.graph.NetworkBuilder;

import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.junit.jupiter.api.Test;

import io.github.jugbot.graph.Vertex;
import io.github.jugbot.graph.Edge;

public class VertexTest {
  @Test
  public void hashes() {
    assertEquals(new Vertex(101), new Vertex(101));
    assertEquals(new Vertex(1), new Vertex(1));
    Block block = MockBlock.Empty();
    assertEquals(new Vertex(block), new Vertex(block));
  }

  @Test
  public void hashCollisions() {
    MutableNetwork<Vertex, Edge> net = NetworkBuilder.directed().build();
    Vertex a = new Vertex(1);
    Vertex b = new Vertex(1);
    net.addNode(a);
    net.addNode(b);
    assertThrows(IllegalArgumentException.class, () -> net.addEdge(a, b, new Edge(1)));
  }
}
