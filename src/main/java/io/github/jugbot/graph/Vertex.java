package io.github.jugbot.graph;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import com.google.common.graph.ElementOrder;
import com.google.common.graph.Network;

import org.bukkit.Chunk;
import org.bukkit.block.Block;

import io.github.jugbot.util.DefaultList;

public class Vertex {
  // hash
  private final int uid;
  // We will need this later
  private final Block block;

  public Optional<Block> getBlock() {
    return Optional.ofNullable(block);
  }

  private Vertex(int uid, Block block) {
    this.block = block;
    this.uid = uid;
  }

  /**
   * Creates a vertex representing a minecraft block
   *
   * @param block unique minecraft object
   * @return Vertex with block hashCode
   */
  public Vertex(@Nonnull Block block) {
    this(Vertex.encode(block), block);
  }

  /**
   * Create a vertex that is not a representative of a minecraft block
   *
   * @param chunk unique minecraft object to make the hash unique for subgraphs
   * @param special reserved indexes for computationally involved vertices
   * @return Vertex with chunk and index amalgam hashCode
   */
  public Vertex(@Nonnull Chunk chunk, int special) {
    this(Vertex.encode(chunk, special), null);
  }

  private static int encode(Block block) {
    return block.hashCode();
  }

  private static int encode(Chunk chunk, int special) {
    return Objects.hash(chunk, special);
  }

  /**
   * Returns a reference in a Network with the same hashCode as the imposter This involves a linear search so should not
   * be used often
   *
   * @param graph Network
   * @param imposter Vertex
   * @return Vertex that exists in a Network
   */
  // public static Optional<Vertex> fromGraph(Network<Vertex, Edge> graph, Vertex imposter) {
  //   // A reminder that important nodes should be early in the order before fetching by linear search
  //   assert (graph.nodeOrder().type() == ElementOrder.Type.INSERTION);
  //   return graph.nodes().stream().filter(imposter::equals).findAny();
  // }

  @Override
  public boolean equals(Object obj) {
    return hashCode() == obj.hashCode();
  }

  @Override
  public int hashCode() {
    return uid;
  }
}
