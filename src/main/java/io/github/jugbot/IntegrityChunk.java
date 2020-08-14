package io.github.jugbot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;

import io.github.jugbot.util.MaxFlow;
import io.github.jugbot.util.MaxFlow.Edge;

public class IntegrityChunk {
  ChunkSnapshot chunk;
  Chunk originalChunk;
  // MaxFlow graph data
  List<MaxFlow.Edge>[] graph;
  int[] dist;
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
    int chunkSize = 16 * 16 * 256;
    src = chunkSize++;
    dest = chunkSize++;
    temp_src = chunkSize++;
    temp_dest = chunkSize++;
    north = chunkSize++;
    east = chunkSize++;
    south = chunkSize++;
    west = chunkSize++;
    nodeCount = chunkSize;
  }

  class XYZ {
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

  private void createVertex(int x, int y, int z, int[] data) {
    XYZ from = new XYZ(x, y, z);
    if (y != 0) {
      XYZ to = new XYZ(x, y - 1, z);
      MaxFlow.createEdge(graph, from.index, to.index, data[DOWN], Edge.Direction.DOWN);
    }
    if (y != 255) {
      XYZ to = new XYZ(x, y + 1, z);
      MaxFlow.createEdge(graph, from.index, to.index, data[UP], Edge.Direction.UP);
    }
    if (x != 0) {
      XYZ to = new XYZ(x - 1, y, z);
      MaxFlow.createEdge(graph, from.index, to.index, data[WEST], Edge.Direction.WEST);
    }
    if (x != 15) {
      XYZ to = new XYZ(x + 1, y, z);
      MaxFlow.createEdge(graph, from.index, to.index, data[EAST], Edge.Direction.EAST);
    }
    if (z != 0) {
      XYZ to = new XYZ(x, y, z - 1);
      MaxFlow.createEdge(graph, from.index, to.index, data[NORTH], Edge.Direction.NORTH);
    }
    if (z != 15) {
      XYZ to = new XYZ(x, y, z + 1);
      MaxFlow.createEdge(graph, from.index, to.index, data[SOUTH], Edge.Direction.SOUTH);
    }
    // Add edge from source to block with capacity of block weight
    MaxFlow.createEdge(graph, src, from.index, data[MASS], Edge.Direction.OTHER);
    // Blocks on the bottom row will connect to the sink
    if (y == 0) {
      MaxFlow.createEdge(graph, from.index, dest, Integer.MAX_VALUE, Edge.Direction.OTHER);
    }
  }

  IntegrityChunk(Chunk liveChunk) {
    graph = MaxFlow.createGraph(nodeCount);
    dist = new int[nodeCount];
    chunk = liveChunk.getChunkSnapshot();
    originalChunk = liveChunk;
    // Translate chunk to graph
    // Note the order of blocks is incremental on the y-axis
    // for some handy computation later
    for (int y = 0; y < 256; y++) {
      for (int x = 0; x < 16; x++) {
        for (int z = 0; z < 16; z++) {
          BlockData block = chunk.getBlockData(x, y, z);
          int[] data = getStructuralData(block);
          if (data == null) continue;
          createVertex(x, y, z, data);
        }
      }
    }
  }

  /** Will undo some flow after removing */
  void removeVertices() {}

  /** Assumes that replacing Block edges have sufficient edge capacity (flow < C) */
  void replaceVertices(Map<Integer, int[]> blockdata) {}

  /** Update chunk with added / removed blocks */
  void update(Block[] blocks) {
    List<Integer> toAdd = new ArrayList<>();
    for (Block block : blocks) {
      Location loc = block.getLocation().subtract(originalChunk.getX(), 0, originalChunk.getZ());
      int index = new XYZ(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()).index;
      int[] data = getStructuralData(block.getBlockData());
      for (Edge e : graph[index]) {
        // how to get existing block-specific edges :/
      }
    }
  }

  public Block[] getIntegrityViolations() {
    // Run Max Flow and get nodes to remove
    MaxFlow.maxFlow(graph, dist, src, dest);
    List<Integer> offending = MaxFlow.getOffendingVertices(graph, dist, src, dest);
    // Translate vertices to Blocks w/ Locations
    Block[] blocks = new Block[offending.size()];
    for (int i = 0; i < offending.size(); i++) {
      XYZ chunkLocation = new XYZ(offending.get(i));
      blocks[i] = (originalChunk.getBlock(chunkLocation.x, chunkLocation.y, chunkLocation.z));
    }
    return blocks;
  }

  public interface Callback<T> {
    void done(T result);
  }

  public static void getBrokenBlocks(final IntegrityChunk chunk, final Callback<Block[]> callback) {
    // Run outside of the tick loop
    Bukkit.getScheduler()
        .runTaskAsynchronously(
            App.Instance(),
            new Runnable() {
              @Override
              public void run() {
                final Block[] result = chunk.getIntegrityViolations();
                // go back to the tick loop
                Bukkit.getScheduler()
                    .runTask(
                        App.Instance(),
                        new Runnable() {
                          @Override
                          public void run() {
                            // call the callback with the result
                            callback.done(result);
                          }
                        });
              }
            });
  }

  private static final int MASS = 0;
  private static final int UP = 1;
  private static final int DOWN = 2;
  private static final int NORTH = 3;
  private static final int EAST = 4;
  private static final int SOUTH = 5;
  private static final int WEST = 6;

  /**
   * @param block
   * @return ["mass", "up", "down", "north", "east", "south", "west"]
   */
  static final int[] getStructuralData(BlockData block) {
    Material material = block.getMaterial();
    if (!material.isSolid()) return null;
    int[] data = Config.Instance().getBlockData().getData(material);
    if (data == null)
      return new int[] {
        1,
        Integer.MAX_VALUE,
        Integer.MAX_VALUE,
        Integer.MAX_VALUE,
        Integer.MAX_VALUE,
        Integer.MAX_VALUE,
        Integer.MAX_VALUE
      };
    return data;
  }
}
