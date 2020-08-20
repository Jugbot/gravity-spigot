package io.github.jugbot;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;

import io.github.jugbot.util.MaxFlow;
import io.github.jugbot.util.MaxFlow.Edge;

public class IntegrityChunk implements Serializable {
  // Chunk association
  private final int x;
  private final int z;
  private final String worldName;
  // Full chunk state to guaruntee no desyncs
  private Material[] snapshot;
  // MaxFlow graph data
  private List<MaxFlow.Edge>[] graph;
  private int[] dist;
  static final int chunkSize;
  static final int nodeCount;
  static final int src;
  static final int dest;
  // For certain graph operations
  static final int temp_src;
  static final int temp_dest;
  // Cached connections to other chunks
  static final int north;
  static final int east;
  static final int south;
  static final int west;

  static {
    chunkSize = 16 * 16 * 256;
    int total = chunkSize;
    src = total++;
    dest = total++;
    temp_src = total++;
    temp_dest = total++;
    north = total++;
    east = total++;
    south = total++;
    west = total++;
    nodeCount = total;
  }

  private static class XYZ {
    public int x, y, z;
    public int index;

    XYZ(int x, int y, int z) {
      this.x = x;
      this.z = z;
      this.y = y;
      this.index = x * 16 * 256 + z * 256 + y;
    }

    XYZ(int index) {
      this.index = index;
      this.y = index % 256;
      index /= 256;
      this.z = index % 16;
      index /= 16;
      this.x = index;
    }
  }

  private static void createVertex(
      List<MaxFlow.Edge>[] graph, int x, int y, int z, EnumMap<IntegrityData, Integer> data) {
    XYZ from = new XYZ(x, y, z);
    if (y != 0) {
      XYZ to = new XYZ(x, y - 1, z);
      MaxFlow.createEdge(graph, from.index, to.index, data.get(IntegrityData.DOWN), IntegrityData.DOWN);
    }
    if (y != 255) {
      XYZ to = new XYZ(x, y + 1, z);
      MaxFlow.createEdge(graph, from.index, to.index, data.get(IntegrityData.UP), IntegrityData.UP);
    }
    if (x != 0) {
      XYZ to = new XYZ(x - 1, y, z);
      MaxFlow.createEdge(graph, from.index, to.index, data.get(IntegrityData.WEST), IntegrityData.WEST);
    }
    if (x != 15) {
      XYZ to = new XYZ(x + 1, y, z);
      MaxFlow.createEdge(graph, from.index, to.index, data.get(IntegrityData.EAST), IntegrityData.EAST);
    }
    if (z != 0) {
      XYZ to = new XYZ(x, y, z - 1);
      MaxFlow.createEdge(graph, from.index, to.index, data.get(IntegrityData.NORTH), IntegrityData.NORTH);
    }
    if (z != 15) {
      XYZ to = new XYZ(x, y, z + 1);
      MaxFlow.createEdge(graph, from.index, to.index, data.get(IntegrityData.SOUTH), IntegrityData.SOUTH);
    }
    // Add edge from source to block with capacity of block weight
    MaxFlow.createEdge(graph, src, from.index, data.get(IntegrityData.MASS), null);
    // Blocks on the bottom row will connect to the sink
    if (y == 0) {
      MaxFlow.createEdge(graph, from.index, dest, Integer.MAX_VALUE, null);
    }
  }

  private void initGraph(ChunkSnapshot chunk) {
    // Translate chunk to graph
    // Note the order of blocks is incremental on the y-axis
    // for some handy computation later
    for (int y = 0; y < 256; y++) {
      for (int x = 0; x < 16; x++) {
        for (int z = 0; z < 16; z++) {
          Material material = chunk.getBlockData(x, y, z).getMaterial();
          snapshot[new XYZ(x, y, z).index] = material;
          EnumMap<IntegrityData, Integer> data = getStructuralData(material);
          if (data == null) continue;
          createVertex(graph, x, y, z, data);
        }
      }
    }
  }

  IntegrityChunk(World world, int x, int z) {
    this(world.getChunkAt(x, z));
  }

  IntegrityChunk(Chunk liveChunk) {
    x = liveChunk.getX();
    z = liveChunk.getZ();
    // System.out.println(x+ " "+z);
    worldName = liveChunk.getWorld().getName();
    graph = MaxFlow.createGraph(nodeCount);
    dist = new int[nodeCount];
    snapshot = new Material[chunkSize];
    ChunkSnapshot chunk = liveChunk.getChunkSnapshot();
    initGraph(chunk);
  }

  /** Update chunk with added / removed blocks */
  void update(ChunkSnapshot newSnapshot) {
    List<int[]> toChange = new ArrayList<>();
    for (int index = 0; index < snapshot.length; index++) {
      XYZ loc = new XYZ(index);
      Material newMaterial = newSnapshot.getBlockType(loc.x, loc.y, loc.z);
      Material oldMaterial = snapshot[index];
      // Update snapshot
      snapshot[index] = newMaterial;
      // If there is no structural change, do nothing
      if (newMaterial == oldMaterial) continue;
      if (!isStructural(oldMaterial) && !isStructural(newMaterial)) continue;
      System.out.println(
          "Change from " + oldMaterial + " to " + newMaterial + " at " + loc.x + ", " + loc.y + ", " + loc.z);
      // Change edge weights to the new data
      EnumMap<IntegrityData, Integer> data = getStructuralData(newMaterial);
      if (data == null) {
        //
      }
      // Record edge weights to be changed
      for (IntegrityData edgeType : data.keySet()) {
        // Existing edge / vertex may not exist
        if (graph[index].get(edgeType.ordinal()) == null) continue;
        toChange.add(new int[] {index, edgeType.ordinal(), data.get(edgeType)});
      }
    }
    // Run super cool algorithm
    MaxFlow.changeEdges(graph, dist, src, dest, toChange, temp_src, temp_dest);
    // Run for edges whoes capacities were set to zero
    // Will only remove if augment capacity is also zero.
    MaxFlow.pruneEdges(graph, dist, src, dest, toChange);
  }

  /** Called Asynchronously */
  public Location[] getIntegrityViolations() {
    // Run Max Flow and get nodes to remove
    MaxFlow.maxFlow(graph, dist, src, dest);
    List<Integer> offending = MaxFlow.getOffendingVertices(graph, dist, src, dest);
    // Translate vertices to Blocks w/ Locations
    Location[] blocks = new Location[offending.size()];
    World world = getWorld();
    for (int i = 0; i < offending.size(); i++) {
      XYZ loc = new XYZ(offending.get(i));
      blocks[i] = new Location(world, loc.x + getBlockX(), loc.y, loc.z + getBlockZ());
    }
    return blocks;
  }

  static EnumMap<IntegrityData, Integer> getStructuralData(Material material) {
    if (!isStructural(material)) return Config.Instance().getBlockData().getEmpty();
    EnumMap<IntegrityData, Integer> data = Config.Instance().getBlockData().getData(material);
    if (data == null) return Config.Instance().getBlockData().getDefault();
    return data;
  }

  static boolean isStructural(Material material) {
    return material.isSolid();
  }

  public int getX() {
    return x;
  }

  public int getZ() {
    return z;
  }

  public int getBlockX() {
    return x * 16;
  }

  public int getBlockZ() {
    return z * 16;
  }

  public World getWorld() {
    return Bukkit.getWorld(worldName);
  }

  public String getWorldName() {
    return worldName;
  }
}
