package io.github.jugbot.graph;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import com.google.common.graph.ElementOrder;
import com.google.common.graph.EndpointPair;
import com.google.common.graph.Graph;
import com.google.common.graph.MutableNetwork;
import com.google.common.graph.NetworkBuilder;

import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.checkerframework.checker.nullness.qual.Nullable;

import io.github.jugbot.App;
import io.github.jugbot.Config;
import io.github.jugbot.IntegrityData;

public class SubGraph implements MutableNetwork<Vertex, Edge> {
  private final Chunk chunk;
  // Full chunk state to guaruntee no desyncs
  private ChunkSnapshot snapshot;
  // MaxFlow graph data
  private MutableNetwork<Vertex, Edge> network =
      NetworkBuilder.directed()
          .nodeOrder(ElementOrder.insertion())
          // .expectedNodeCount(16 * 16 * 256 + 2 * 16 * 256 + 256)
          // .expectedEdgeCount((16 * 16 * 256 + 2 * 16 * 256 + 256) * 6)
          .build();

  // consts
  private Vertex src;
  private Vertex dest;
  // Utility nodes for cached connections to other chunks
  private Vertex north_src;
  private Vertex east_src;
  private Vertex south_src;
  private Vertex west_src;
  private Vertex north_dest;
  private Vertex east_dest;
  private Vertex south_dest;
  private Vertex west_dest;

  private GraphState state = new GraphState();;

  public Map<Vertex, Integer> dists = new HashMap<>();

  public SubGraph(Chunk liveChunk) {
    chunk = liveChunk;
    snapshot = liveChunk.getChunkSnapshot();
    // App.Instance().getLogger().fine(x+ " "+z);

    // Initialize special vertices
    // MaxFlow source / sink
    src = new Vertex(liveChunk, ReservedID.SOURCE);
    dest = new Vertex(liveChunk, ReservedID.DEST);
    // Cached connections utility nodes
    north_src = new Vertex(liveChunk, ReservedID.NORTH_SOURCE);
    east_src = new Vertex(liveChunk, ReservedID.EAST_SOURCE);
    south_src = new Vertex(liveChunk, ReservedID.SOUTH_SOURCE);
    west_src = new Vertex(liveChunk, ReservedID.WEST_SOURCE);

    north_dest = new Vertex(liveChunk, ReservedID.NORTH_DEST);
    east_dest = new Vertex(liveChunk, ReservedID.EAST_DEST);
    south_dest = new Vertex(liveChunk, ReservedID.SOUTH_DEST);
    west_dest = new Vertex(liveChunk, ReservedID.WEST_DEST);

    MaxFlow.createEdge(network, src, north_src, Float.POSITIVE_INFINITY);
    MaxFlow.createEdge(network, src, east_src, Float.POSITIVE_INFINITY);
    MaxFlow.createEdge(network, src, south_src, Float.POSITIVE_INFINITY);
    MaxFlow.createEdge(network, src, west_src, Float.POSITIVE_INFINITY);

    MaxFlow.createEdge(network, north_dest, dest, Float.POSITIVE_INFINITY);
    MaxFlow.createEdge(network, east_dest, dest, Float.POSITIVE_INFINITY);
    MaxFlow.createEdge(network, south_dest, dest, Float.POSITIVE_INFINITY);
    MaxFlow.createEdge(network, west_dest, dest, Float.POSITIVE_INFINITY);

    // Translate chunk to graph
    // Note the order of blocks is incremental on the y-axis
    // for some handy computation later
    for (int y = 0; y < 256; y++) {
      for (int x = 0; x < 16; x++) {
        for (int z = 0; z < 16; z++) {
          Material material = snapshot.getBlockData(x, y, z).getMaterial();
          EnumMap<IntegrityData, Float> data = getStructuralData(material);
          createVertex(liveChunk.getBlock(x, y, z), data);
        }
      }
    }
    MaxFlow.maxFlow(this, dists, src, dest);
  }

  private void createVertex(Block block, EnumMap<IntegrityData, Float> data) {
    Vertex fromIndex = new Vertex(block);
    int x = block.getX();
    int y = block.getY();
    int z = block.getZ();
    Vertex toIndex;
    // Connect blocks to each other
    if (y != 0) {
      toIndex = new Vertex(block.getRelative(BlockFace.DOWN));
      MaxFlow.createEdge(network, fromIndex, toIndex, data.get(IntegrityData.DOWN));
    }
    if (y != 255) {
      toIndex = new Vertex(block.getRelative(BlockFace.UP));
      MaxFlow.createEdge(network, fromIndex, toIndex, data.get(IntegrityData.UP));
    }
    // Changes must reflect in update()
    if (x > 0) {
      toIndex = new Vertex(block.getRelative(BlockFace.WEST));
      MaxFlow.createEdge(network, fromIndex, toIndex, data.get(IntegrityData.WEST));
    }
    if (z > 0) {
      toIndex = new Vertex(block.getRelative(BlockFace.NORTH));
      MaxFlow.createEdge(network, fromIndex, toIndex, data.get(IntegrityData.NORTH));
    }
    // For SE directions, the edges that lead off-chunk will be created
    // This is for later computation of multi-chunk integrity
    // so there are no duplicate edges
    toIndex = new Vertex(block.getRelative(BlockFace.SOUTH));
    MaxFlow.createEdge(network, fromIndex, toIndex, data.get(IntegrityData.SOUTH));
    MaxFlow.createEdge(
        network,
        fromIndex,
        toIndex,
        getStructuralData(block.getRelative(BlockFace.SOUTH).getType()).get(IntegrityData.NORTH));

    toIndex = new Vertex(block.getRelative(BlockFace.EAST));
    MaxFlow.createEdge(network, fromIndex, toIndex, data.get(IntegrityData.EAST));
    MaxFlow.createEdge(
        network,
        fromIndex,
        toIndex,
        getStructuralData(block.getRelative(BlockFace.EAST).getType()).get(IntegrityData.WEST));

    // Connect edges for cached flows from other chunks
    if (x == 0) {
      MaxFlow.createEdge(network, fromIndex, west_dest, 0);
      MaxFlow.createEdge(network, west_src, fromIndex, 0);
    }
    if (x == 15) {
      MaxFlow.createEdge(network, fromIndex, east_dest, 0);
      MaxFlow.createEdge(network, east_src, fromIndex, 0);
    }
    if (z == 0) {
      MaxFlow.createEdge(network, fromIndex, north_dest, 0);
      MaxFlow.createEdge(network, north_src, fromIndex, 0);
    }
    if (z == 15) {
      MaxFlow.createEdge(network, fromIndex, south_dest, 0);
      MaxFlow.createEdge(network, south_src, fromIndex, 0);
    }

    // Add edge from source to block with capacity of block weight
    MaxFlow.createEdge(network, src, fromIndex, data.get(IntegrityData.MASS));
    // Blocks on the bottom row will connect to the sink
    if (y == 0) {
      MaxFlow.createEdge(network, fromIndex, dest, Integer.MAX_VALUE);
    }
  }

  /** Update chunk with added / removed blocks Significant speed improvement compared to re-creation */
  public GraphState update(ChunkSnapshot newSnapshot) {
    Map<EndpointPair<Vertex>, Float> toChange = new HashMap<>();

    for (int y = 0; y < 256; y++) {
      for (int x = 0; x < 16; x++) {
        for (int z = 0; z < 16; z++) {
          Material newMaterial = newSnapshot.getBlockType(x, y, z);
          Material oldMaterial = snapshot.getBlockType(x, y, z);
          // If there is no structural change, do nothing
          if (newMaterial == oldMaterial) continue;
          if (!isStructural(oldMaterial) && !isStructural(newMaterial)) continue;
          // Change edge weights to the new data
          EnumMap<IntegrityData, Float> data = getStructuralData(newMaterial);
          // Record edge weights to be changed
          Block block = chunk.getBlock(x, y, z);
          Vertex reference = new Vertex(block);
          toChange.put(EndpointPair.ordered(src, reference), data.get(IntegrityData.MASS));
          if (y != 255) {
            toChange.put(
                EndpointPair.ordered(reference, new Vertex(block.getRelative(BlockFace.UP))),
                data.get(IntegrityData.UP));
          }
          if (y != 0) {
            toChange.put(
                EndpointPair.ordered(reference, new Vertex(block.getRelative(BlockFace.DOWN))),
                data.get(IntegrityData.DOWN));
          }
          if (z > 0) {
            toChange.put(
                EndpointPair.ordered(reference, new Vertex(block.getRelative(BlockFace.NORTH))),
                data.get(IntegrityData.NORTH));
          }
          if (x > 0) {
            toChange.put(
                EndpointPair.ordered(reference, new Vertex(block.getRelative(BlockFace.WEST))),
                data.get(IntegrityData.WEST));
          }
          toChange.put(
              EndpointPair.ordered(reference, new Vertex(block.getRelative(BlockFace.EAST))),
              data.get(IntegrityData.EAST));
          toChange.put(
              EndpointPair.ordered(reference, new Vertex(block.getRelative(BlockFace.SOUTH))),
              data.get(IntegrityData.SOUTH));
        }
      }
    }
    // update snapshot
    snapshot = newSnapshot;
    // Run super cool algorithm
    MaxFlow.changeEdges(network, dists, src, dest, toChange);
    // Run for edges whoes capacities were set to zero
    // Will only remove if augment capacity is also zero.
    this.state = MaxFlow.getGraphState(network, dists, src, dest);
    return this.state;
  }

  /** Called Asynchronously */
  public Block[] getIntegrityViolations() {
    List<Vertex> offending = this.state.offendingNodes;
    // Translate vertices to Blocks w/ Locations
    return offending.stream()
        .map(v -> v.getBlockXYZ())
        .filter(o -> o.isPresent())
        .map(
            o -> {
              int[] xyz = o.get();
              // TODO: send BlockData instead to verify the block being broken
              return this.chunk.getBlock(xyz[0] & 0xF, xyz[1] & 0xFF, xyz[2] & 0xF);
            })
        .toArray(Block[]::new);
  }

  static EnumMap<IntegrityData, Float> getStructuralData(Material material) {
    if (!isStructural(material)) return Config.Instance().getBlockData().getEmpty();
    EnumMap<IntegrityData, Float> data = Config.Instance().getBlockData().getData(material);
    if (data == null) return Config.Instance().getBlockData().getDefault();
    return data;
  }

  public GraphState getState() {
    if (this.state == null) this.state = new GraphState();
    return this.state;
  }

  static boolean isStructural(Material material) {
    return material.isSolid();
  }

  public int getX() {
    return chunk.getX();
  }

  public int getZ() {
    return chunk.getZ();
  }

  public int getBlockX() {
    return getX() * 16;
  }

  public int getBlockZ() {
    return getZ() * 16;
  }

  public World getWorld() {
    return chunk.getWorld();
  }

  public String getWorldName() {
    return getWorld().getName();
  }

  public Set<Edge> debugGetEdgesAt(Block block) {
    return network.outEdges(new Vertex(block));
  }

  @Override
  public int hashCode() {
    return Objects.hash(getWorldName(), getX(), getZ());
  }

  @Override
  public Set<Vertex> nodes() {
    return network.nodes();
  }

  @Override
  public Set<Edge> edges() {
    return network.edges();
  }

  @Override
  public Graph<Vertex> asGraph() {
    return network.asGraph();
  }

  @Override
  public boolean isDirected() {
    return network.isDirected();
  }

  @Override
  public boolean allowsParallelEdges() {
    return network.allowsParallelEdges();
  }

  @Override
  public boolean allowsSelfLoops() {
    return network.allowsSelfLoops();
  }

  @Override
  public ElementOrder<Vertex> nodeOrder() {
    return network.nodeOrder();
  }

  @Override
  public ElementOrder<Edge> edgeOrder() {
    return network.edgeOrder();
  }

  @Override
  public Set<Vertex> adjacentNodes(Vertex node) {
    return network.adjacentNodes(node);
  }

  @Override
  public Set<Vertex> predecessors(Vertex node) {
    return network.predecessors(node);
  }

  @Override
  public Set<Vertex> successors(Vertex node) {
    return network.successors(node);
  }

  @Override
  public Set<Edge> incidentEdges(Vertex node) {
    return network.incidentEdges(node);
  }

  @Override
  public Set<Edge> inEdges(Vertex node) {
    return network.inEdges(node);
  }

  @Override
  public Set<Edge> outEdges(Vertex node) {
    return network.outEdges(node);
  }

  @Override
  public int degree(Vertex node) {
    return network.degree(node);
  }

  @Override
  public int inDegree(Vertex node) {
    return network.inDegree(node);
  }

  @Override
  public int outDegree(Vertex node) {
    return network.outDegree(node);
  }

  @Override
  public EndpointPair<Vertex> incidentNodes(Edge edge) {
    return network.incidentNodes(edge);
  }

  @Override
  public Set<Edge> adjacentEdges(Edge edge) {
    return network.adjacentEdges(edge);
  }

  @Override
  public Set<Edge> edgesConnecting(Vertex nodeU, Vertex nodeV) {
    return network.edgesConnecting(nodeU, nodeV);
  }

  @Override
  public Set<Edge> edgesConnecting(EndpointPair<Vertex> endpoints) {
    return network.edgesConnecting(endpoints);
  }

  @Override
  public Optional<Edge> edgeConnecting(Vertex nodeU, Vertex nodeV) {
    return network.edgeConnecting(nodeU, nodeV);
  }

  @Override
  public Optional<Edge> edgeConnecting(EndpointPair<Vertex> endpoints) {
    return network.edgeConnecting(endpoints);
  }

  @Override
  public @Nullable Edge edgeConnectingOrNull(Vertex nodeU, Vertex nodeV) {
    return network.edgeConnectingOrNull(nodeU, nodeV);
  }

  @Override
  public @Nullable Edge edgeConnectingOrNull(EndpointPair<Vertex> endpoints) {
    return network.edgeConnectingOrNull(endpoints);
  }

  @Override
  public boolean hasEdgeConnecting(Vertex nodeU, Vertex nodeV) {
    return network.hasEdgeConnecting(nodeU, nodeV);
  }

  @Override
  public boolean hasEdgeConnecting(EndpointPair<Vertex> endpoints) {
    return network.hasEdgeConnecting(endpoints);
  }

  @Override
  public boolean addNode(Vertex node) {
    return network.addNode(node);
  }

  @Override
  public boolean addEdge(Vertex nodeU, Vertex nodeV, Edge edge) {
    return network.addEdge(nodeU, nodeV, edge);
  }

  @Override
  public boolean addEdge(EndpointPair<Vertex> endpoints, Edge edge) {
    return network.addEdge(endpoints, edge);
  }

  @Override
  public boolean removeNode(Vertex node) {
    return network.removeNode(node);
  }

  @Override
  public boolean removeEdge(Edge edge) {
    return network.removeEdge(edge);
  }
}
