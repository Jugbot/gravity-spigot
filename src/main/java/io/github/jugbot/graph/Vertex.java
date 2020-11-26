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
    this.uid = Integer.rotateLeft(this.yOrSpecial, 24) ^ Integer.rotateLeft(this.x, 12) ^ this.z;
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
    this.uid = Integer.rotateLeft(this.yOrSpecial, 24) ^ Integer.rotateLeft(this.x, 12) ^ this.z;
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
    this.uid = Integer.rotateLeft(this.yOrSpecial, 24) ^ Integer.rotateLeft(this.x, 12) ^ this.z;
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
}
