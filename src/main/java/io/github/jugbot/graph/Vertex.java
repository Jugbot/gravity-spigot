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
  private final int x;
  // special indices occupy the negative range
  private final int yOrSpecial;
  private final int z;

  public Optional<int[]> getBlockXYZ() {
    if (yOrSpecial < 0) return Optional.empty();
    return Optional.of(new int[] {x, yOrSpecial, z});
  }

  public Optional<int[]> getChunkXZ() {
    if (yOrSpecial >= 0) return Optional.empty();
    return Optional.of(new int[] {x, z});
  }

  /**
   * Creates a vertex representing a minecraft block
   *
   * @param block unique minecraft object
   * @return Vertex with block hashCode
   */
  public Vertex(@Nonnull Block block) {
    this.x = block.getX();
    this.yOrSpecial = block.getY();
    this.z = block.getZ();
  }

  /**
   * Create a vertex that is not a representative of a minecraft block and is specific to this subgraph
   *
   * @param chunk unique minecraft object to make the hash unique for subgraphs
   * @param special reserved indexes for computationally involved vertices
   * @return Vertex with chunk and index amalgam hashCode
   */
  public Vertex(@Nonnull Chunk chunk, int special) {
    if (special <= 0) throw new IllegalArgumentException();
    this.yOrSpecial = -special;
    this.x = chunk.getX();
    this.z = chunk.getZ();
  }

  /**
   * Create an abstract vertex with its identity defined by a special number
   *
   * @param special reserved indexes for computationally involved vertices
   * @return Vertex with hashcode defined by a special number
   */
  public Vertex(int special) {
    if (special <= 0) throw new IllegalArgumentException();
    this.yOrSpecial = -special;
    this.x = 0;
    this.z = 0;
  }

  private static int encode(Block block) {
    // this.y << 24 ^ this.x ^ this.z ^ this.getWorld().hashCode();
    return block.hashCode();
  }

  // this needs to be unique for equals() to work
  // alternatively, store x, y/s, z, worldhash for equals()
  // the world doesn't need to be stored (that gets handled higher up)
  private static int encode(Chunk chunk, int special) {
    // if (chunk == null) return special;
    // return Objects.hash(special, chunk);
    return special << 24 ^ chunk.getX() ^ chunk.getZ() ^ chunk.getWorld().hashCode();
  }

  /**
   * Returns a reference in a Network with the same hashCode as the imposter This involves a linear search so should not
   * be used often
   *
   * @param graph Network
   * @param imposter Vertex
   * @return Vertex that exists in a Network
   */
  // public static Optional<Vertex> fromGraph(Network<Vertex, Edge> graph, Vertex
  // imposter) {
  // // A reminder that important nodes should be early in the order before
  // fetching by linear search
  // assert (graph.nodeOrder().type() == ElementOrder.Type.INSERTION);
  // return graph.nodes().stream().filter(imposter::equals).findAny();
  // }

  @Override
  public boolean equals(Object o) {
    if (o == this) return true;
    if (!(o instanceof Vertex)) return false;
    Vertex other = (Vertex) o;
    return other.x == this.x && other.yOrSpecial == this.yOrSpecial && other.z == this.z;
  }

  @Override
  public int hashCode() {
    return this.yOrSpecial ^ this.x ^ this.z;
  }
}
