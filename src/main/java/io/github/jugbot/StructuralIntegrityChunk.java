package io.github.jugbot;

import java.util.List;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;

public class StructuralIntegrityChunk extends NotificationThread<Block[]> {
  Chunk chunk;
  List<MaxFlow.Edge>[] graph;
  int src;
  int dest;

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

  StructuralIntegrityChunk(Chunk chunk) {
    int nodeCount = 16*16*256+2;
    src = nodeCount - 1;
    dest = nodeCount - 2;
    graph = MaxFlow.createGraph(nodeCount);
    this.chunk = chunk;
    // Translate chunk to graph 
    for (int x = 0; x < 16; x++) {
      for (int z = 0; z < 16; z++) {
        for (int y = 0; y < 256; y++) {
          BlockData block = chunk.getChunkSnapshot().getBlockData(x, y, z);
          int[] data = getStructuralData(block);
          if (data == null) continue;
          XYZ from = new XYZ(x,y,z);
          if (y != 0) {
            XYZ to = new XYZ(x, y-1, z);
            MaxFlow.addEdge(graph, from.index, to.index, data[DOWN]);
          }
          if (y != 255) {
            XYZ to = new XYZ(x, y+1, z);
            MaxFlow.addEdge(graph, from.index, to.index, data[UP]);
          }
          if (x != 0) {
            XYZ to = new XYZ(x-1, y ,z);
            MaxFlow.addEdge(graph, from.index, to.index, data[WEST]);
          }
          if (x != 15) {
            XYZ to = new XYZ(x+1, y ,z);
            MaxFlow.addEdge(graph, from.index, to.index, data[EAST]);
          }
          if (z != 0) {
            XYZ to = new XYZ(x, y ,z-1);
            MaxFlow.addEdge(graph, from.index, to.index, data[NORTH]);
          }
          if (z != 15) {
            XYZ to = new XYZ(x, y ,z+1);
            MaxFlow.addEdge(graph, from.index, to.index, data[SOUTH]);
          }
          MaxFlow.addEdge(graph, src, from.index, data[MASS]);
          if (y == 0) {
            MaxFlow.addEdge(graph, from.index, dest, Integer.MAX_VALUE);
          }
        }
      }
    }
    
  }

  @Override
  public Block[] doWork() {
    // Run Max Flow and get nodes to remove
    MaxFlow.maxFlow(graph, src, dest);
    List<Integer> offending = MaxFlow.getOffendingVertices(graph, src, dest);
    // Translate vertices to Blocks w/ Locations
    Block[] blocks = new Block[offending.size()];
    for (int i = 0; i < offending.size(); i++) {
      XYZ chunkLocation = new XYZ(offending.get(i));
      blocks[i] = (chunk.getBlock(chunkLocation.x, chunkLocation.y, chunkLocation.z));
    }
    return blocks;
  }

  private static final int MASS = 0;
  private static final int UP = 1;
  private static final int DOWN = 2;
  private static final int NORTH = 3;
  private static final int EAST = 4;
  private static final int SOUTH = 5;
  private static final int WEST = 6;

  /**
   * 
   * @param block
   * @return ["mass", "up", "down", "north", "east", "south", "west"]
   */
  static int[] getStructuralData(BlockData block) {
    Material material = block.getMaterial();
    if (!material.isSolid()) return null;
    switch (material) {
      case OAK_PLANKS:
        return new int[]{1, 5, 2, 3, 3, 3, 3};
      case OAK_LOG:
        // Example directional block
        Directional direction = (Directional) block;
        switch (direction.getFacing()) {
          case DOWN:
            break;
          case EAST:
            break;
          case EAST_NORTH_EAST:
            break;
          case EAST_SOUTH_EAST:
            break;
          case NORTH:
            break;
          case NORTH_EAST:
            break;
          case NORTH_NORTH_EAST:
            break;
          case NORTH_NORTH_WEST:
            break;
          case NORTH_WEST:
            break;
          case SELF:
            break;
          case SOUTH:
            break;
          case SOUTH_EAST:
            break;
          case SOUTH_SOUTH_EAST:
            break;
          case SOUTH_SOUTH_WEST:
            break;
          case SOUTH_WEST:
            break;
          case UP:
            break;
          case WEST:
            break;
          case WEST_NORTH_WEST:
            break;
          case WEST_SOUTH_WEST:
            break;
          default:
            break;
        }
      default:
        // Default mass 1, Infinite integrity
        return new int[] {
          1,
          Integer.MAX_VALUE,
          Integer.MAX_VALUE,
          Integer.MAX_VALUE,
          Integer.MAX_VALUE,
          Integer.MAX_VALUE,
          Integer.MAX_VALUE,
        };
    }
  }
}