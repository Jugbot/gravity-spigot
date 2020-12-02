package io.github.jugbot.graph;

import java.util.Optional;

import javax.annotation.Nonnull;

import org.bukkit.Chunk;
import org.bukkit.block.Block;

public class Vertex {
  private final int x;
  // special indices occupy the negative range
  private final int yOrSpecial;
  private final int z;
  // precomputed hash
  private final int uid;

  public boolean isBlock() {
    return yOrSpecial >= 0;
  }

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
    this(block.getX(), block.getY(), block.getZ());
  }

  /**
   * Create a vertex that is not a representative of a minecraft block and is specific to this subgraph
   *
   * @param chunk unique minecraft object to make the hash unique for subgraphs
   * @param special reserved indexes for computationally involved vertices
   * @return Vertex with chunk and index amalgam hashCode
   */
  public Vertex(@Nonnull Chunk chunk, ReservedID reserved) {
    this(chunk.getX(), -reserved.value(), chunk.getZ());
    if (reserved.value() <= 0) throw new IllegalArgumentException();
  }

  /**
   * Create an abstract vertex with its identity defined by a special number
   *
   * @param special reserved indexes for computationally involved vertices
   * @return Vertex with hashcode defined by a special number
   */
  public Vertex(ReservedID reserved) {
    this(0, -reserved.value(), 0);
    if (reserved.value() <= 0) throw new IllegalArgumentException();
  }

  /**
   * Exposed for testing purposes.
   *
   * @param x Block location or Chunk location
   * @param y Block location or Special value
   * @param z Block location or Chunk location
   */
  protected Vertex(int x, int y, int z) {
    this.yOrSpecial = y;
    this.x = x;
    this.z = z;
    this.uid = Integer.rotateLeft(y, 24) ^ Integer.rotateLeft(x, 12) ^ z;
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) return true;
    if (!(o instanceof Vertex)) return false;
    Vertex other = (Vertex) o;
    return other.x == this.x && other.yOrSpecial == this.yOrSpecial && other.z == this.z;
  }

  @Override
  public int hashCode() {
    // return this.yOrSpecial ^ this.x ^ this.z;
    // return String.format("%x|%x|%x",this.yOrSpecial , this.x , this.z).hashCode();
    // return (this.yOrSpecial + this.x + this.z) * 31 + this.yOrSpecial;
    // return Integer.rotateLeft(this.yOrSpecial, 24) ^ Integer.rotateLeft(this.x, 12) ^ this.z;
    return uid;
  }

  @Override
  public String toString() {
    return String.format("Vertex(%d, %d, %d, uid:%x)", x, yOrSpecial, z, uid);
  }
}
