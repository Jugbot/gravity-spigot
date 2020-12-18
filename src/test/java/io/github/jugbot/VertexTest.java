package io.github.jugbot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.common.graph.MutableNetwork;
import com.google.common.graph.NetworkBuilder;

import org.bukkit.block.Block;
import org.junit.jupiter.api.Test;

import io.github.jugbot.graph.Vertex;
import io.github.jugbot.graph.Edge;
import io.github.jugbot.graph.ReservedID;

public class VertexTest {
  @Test
  public void hashes() {
    assertEquals(new Vertex(ReservedID.SOURCE), new Vertex(ReservedID.SOURCE));
    Block block = MockBlock.Empty();
    assertEquals(new Vertex(block), new Vertex(block));
  }

  @Test
  public void hashCollisions() {
    MutableNetwork<Vertex, Edge> net = NetworkBuilder.directed().build();
    Vertex a = new Vertex(ReservedID.NORTH_DEST);
    Vertex b = new Vertex(ReservedID.NORTH_DEST);
    net.addNode(a);
    net.addNode(b);
    assertThrows(IllegalArgumentException.class, () -> net.addEdge(a, b, new Edge(1)));
  }
}
