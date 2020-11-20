package io.github.jugbot;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.bukkit.block.Block;
import org.junit.jupiter.api.Test;

import io.github.jugbot.graph.Vertex;

public class VertexTest {
  @Test
  public void hashes() {
    assertEquals(new Vertex(null, 101), new Vertex(null, 101));
    assertEquals(new Vertex(null, 0), new Vertex(null, 0));
    Block block = new BlockFixture();
    assertEquals(new Vertex(block), new Vertex(block));
  }
}
