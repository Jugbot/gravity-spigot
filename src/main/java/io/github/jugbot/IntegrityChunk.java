package io.github.jugbot;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
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

import io.github.jugbot.util.Constants;
import io.github.jugbot.util.Graph;
import io.github.jugbot.graph.MaxFlow;
import io.github.jugbot.graph.Edge;
import io.github.jugbot.util.DefaultList;

public class IntegrityChunk implements Serializable {
  // Chunk association
  private final int x;
  private final int z;
  private final String worldName;
  // Full chunk state to guaruntee no desyncs
  private Material[] snapshot;
  // MaxFlow graph data
  private DefaultList<List<Edge>> graph;
  private int[] dist;
  static final int chunkSize;
  static final int nodeCount;
  static final int src;
  static final int dest;
  // For certain graph operations
  static final int temp_src;
  static final int temp_dest;
  // Utility nodes for cached connections to other chunks
  static final int north_src;
  static final int east_src;
  static final int south_src;
  static final int west_src;
  static final int north_dest;
  static final int east_dest;
  static final int south_dest;
  static final int west_dest;

  static {
    chunkSize = 16 * 16 * 256;
    int total = chunkSize;
    src = total++;
    dest = total++;
    temp_src = total++;
    temp_dest = total++;
    north_src = total++;
    east_src = total++;
    south_src = total++;
    west_src = total++;
    north_dest = total++;
    east_dest = total++;
    south_dest = total++;
    west_dest = total++;
    nodeCount = total;
  }

  IntegrityChunk(Chunk liveChunk) {
    x = liveChunk.getX();
    z = liveChunk.getZ();
    // App.Instance().getLogger().fine(x+ " "+z);
    worldName = liveChunk.getWorld().getName();
    graph = new DefaultList<List<Edge>>(new List[nodeCount], 
      new ArrayList<>(Collections.nCopies(IntegrityData.values().length, null))
    );
    dist = new int[nodeCount];
    snapshot = new Material[chunkSize];
    ChunkSnapshot chunk = liveChunk.getChunkSnapshot();
    // Translate chunk to graph
    // Note the order of blocks is incremental on the y-axis
    // for some handy computation later
    for (int y = 0; y < 256; y++) {
      for (int x = 0; x < 16; x++) {
        for (int z = 0; z < 16; z++) {
          Material material = chunk.getBlockData(x, y, z).getMaterial();
          int index = index(x, y, z);
          snapshot[index] = material;
          EnumMap<IntegrityData, Integer> data = getStructuralData(material);
          if (data == null) continue;
          createVertex(graph, x, y, z, data);
        }
      }
    }
  }

  private static void createVertex(
      List<List<Edge>> graph, int x, int y, int z, EnumMap<IntegrityData, Integer> data) {
    int fromIndex = index(x, y, z);
    int toIndex;
    if (y != 0) {
      toIndex = index(x, y - 1, z);
      MaxFlow.createEdge(graph, fromIndex, toIndex, data.get(IntegrityData.DOWN), IntegrityData.DOWN);
    }
    if (y != 255) {
      toIndex = index(x, y + 1, z);
      MaxFlow.createEdge(graph, fromIndex, toIndex, data.get(IntegrityData.UP), IntegrityData.UP);
    }
    // For NESW directions, the edges that lead off-chunk will be created pointed at a dummy index
    // This is for later computation of multi-chunk integrity
    if (x != 0) {
      toIndex = index(x - 1, y, z);
    } else {
      toIndex = -index(x - 1, y, z);
    }
    MaxFlow.createEdge(graph, fromIndex, toIndex, data.get(IntegrityData.WEST), IntegrityData.WEST);
    if (x != 15) {
      toIndex = index(x + 1, y, z);
    } else {
      toIndex = -index(x + 1, y, z);
    }
    MaxFlow.createEdge(graph, fromIndex, toIndex, data.get(IntegrityData.EAST), IntegrityData.EAST);
    if (z != 0) {
      toIndex = index(x, y, z - 1);
    } else {
      toIndex = index(x, y, z);
    }
    MaxFlow.createEdge(graph, fromIndex, toIndex, data.get(IntegrityData.NORTH), IntegrityData.NORTH);
    if (z != 15) {
      toIndex = index(x, y, z + 1);
    } else {
      toIndex = -index(x, y, z + 1);
    }
    MaxFlow.createEdge(graph, fromIndex, toIndex, data.get(IntegrityData.SOUTH), IntegrityData.SOUTH);
    // Add edge from source to block with capacity of block weight
    MaxFlow.createEdge(graph, src, fromIndex, data.get(IntegrityData.MASS), IntegrityData.MASS);
    // Blocks on the bottom row will connect to the sink
    if (y == 0) {
      MaxFlow.createEdge(graph, fromIndex, dest, Integer.MAX_VALUE);
    }
  }

  /** Update chunk with added / removed blocks Significant speed improvement compared to re-creation */
  void update(ChunkSnapshot newSnapshot) {
    List<int[]> toChange = new ArrayList<>();
    for (int index = 0; index < snapshot.length; index++) {
      int x = x(index);
      int y = y(index);
      int z = z(index);
      Material newMaterial = newSnapshot.getBlockType(x, y, z);
      Material oldMaterial = snapshot[index];
      // Update snapshot
      snapshot[index] = newMaterial;
      // If there is no structural change, do nothing
      if (newMaterial == oldMaterial) continue;
      if (!isStructural(oldMaterial) && !isStructural(newMaterial)) continue;
      App.Instance()
          .getLogger()
          .fine("Change from " + oldMaterial + " to " + newMaterial + " at " + x + ", " + y + ", " + z);
      // Change edge weights to the new data
      EnumMap<IntegrityData, Integer> data = getStructuralData(newMaterial);
      // Record edge weights to be changed
      for (IntegrityData edgeType : data.keySet()) {
        // Existing edge / vertex may not exist
        if (graph.get(index).get(edgeType.ordinal()) == null) {
          App.Instance().getLogger().fine("Null edge: " + edgeType);
          continue;
        }
        if (edgeType == IntegrityData.MASS) {
          toChange.add(new int[] {src, graph.get(index).get(edgeType.ordinal()).rev, data.get(edgeType)});
        } else {
          toChange.add(new int[] {index, edgeType.ordinal(), data.get(edgeType)});
        }
      }
    }
    // Run super cool algorithm
    MaxFlow.changeEdges(graph, dist, src, dest, toChange, temp_src, temp_dest);
    // Run for edges whoes capacities were set to zero
    // Will only remove if augment capacity is also zero.
    // MaxFlow.pruneEdges(graph, dist, src, dest, toChange);
  }

  /** Called Asynchronously */
  public Location[] getIntegrityViolations() {
    List<Integer> offending = MaxFlow.getOffendingVertices(graph, dist, src, dest);
    // Translate vertices to Blocks w/ Locations
    Location[] blocks = new Location[offending.size()];
    World world = getWorld();
    for (int i = 0; i < offending.size(); i++) {
      int index = offending.get(i);
      blocks[i] = new Location(world, x(index) + getBlockX(), y(index), z(index) + getBlockZ());
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

  public List<Edge> debugGetEdgesAt(Block block) {
    return graph.get(index(block.getX() - getBlockX(), block.getY(), block.getZ() - getBlockZ()));
  }

  public static int index(int x, int y, int z) {
    return x * 16 * 256 + z * 256 + y;
  }

  public static int x(int index) {
    return index / 256 / 16;
  }

  public static int z(int index) {
    return index / 256 % 16;
  }

  public static int y(int index) {
    return index % 256;
  }

  // public static class XYZ {
  //   public int x, y, z;
  //   public int index;

  //   private static int convert3to1(int bx, int bz, int by) {
  //     return (bx + 1) + (by + 1) * 3 + (bz + 1) * 9;
  //   } 

  //   private static int[] convert1to3(int index) {
  //     int z = (index / 9) - 1;
  //     index %= 9;
  //     int y = (index / 3) - 1;
  //     index %= 3;
  //     int x = index;
  //     return new int[] {x, y, z};
  //   }

  //   public XYZ(int x, int y, int z) {
  //     this.x = x;
  //     this.z = z;
  //     this.y = y;
  //     int bx = Math.floorDiv(x, Constants.MX);
  //     int by = Math.floorDiv(y, Constants.MY);
  //     int bz = Math.floorDiv(z, Constants.MZ);
  //     this = x * 16 * 256 + z * 256 + y + convert3to1(bx, bz, by) * Constants.MIndex;
  //   }

  //   public XYZ(int index) {
  //     this = index;
  //     int bindex = Math.floorDiv(index, Constants.MIndex);
  //     int[] offset = convert1to3(bindex);
  //     this.y = index % 256 + offset[1] * Constants.MY;
  //     index /= 256;
  //     this.z = index % 16 + offset[2] * Constants.MZ;
  //     index /= 16;
  //     this.x = index + offset[0] * Constants.MX;
  //   }
  // }
}
