package io.github.jugbot.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.base.Function;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.collect.Streams;
import com.google.common.collect.Table;
import com.google.common.graph.AbstractNetwork;
import com.google.common.graph.ElementOrder;
import com.google.common.graph.EndpointPair;
import com.google.common.graph.ImmutableNetwork;
import com.google.common.graph.MutableNetwork;
import com.google.common.graph.Network;
import com.google.common.graph.NetworkBuilder;
import com.google.common.math.IntMath;

import org.apache.commons.lang.NotImplementedException;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import io.github.jugbot.Asserts;
import io.github.jugbot.Config;
import io.github.jugbot.Integrity;
import io.github.jugbot.util.Cardinal;
import io.github.jugbot.util.IntegerXYZ;
import io.github.jugbot.util.IntegerXZ;

public class SuperGraph extends ForwardingMutableNetwork<Vertex, Edge> {
  // graphs stored by chunk coordinates
  private Table<Integer, Integer, SubGraph> subgraphGrid = HashBasedTable.create();
  private MutableNetwork<Vertex, Edge> network = NetworkBuilder.directed().build();
  public Map<Vertex, Integer> dists = new HashMap<>();
  public final Vertex src = new Vertex(ReservedID.SUPER_SOURCE);
  public final Vertex dest = new Vertex(ReservedID.SUPER_DEST);

  private GraphState state;
  private int iteration = 0;

  private World world;
  private IntegerXZ chunkOrigin;

  public SuperGraph(SubGraph starter) {
    this(starter.getWorld());
    this.chunkOrigin = new IntegerXZ(starter.getX(), starter.getZ());
    this.state = new GraphState();
    this.state.dependantChunks = starter.getState().dependantChunks;
  }

  public SuperGraph(World world) {
    this.addNode(src);
    this.addNode(dest);
    this.world = world;
  }

  private Map<EndpointPair<Vertex>, Float> addOne(SubGraph subgraph) {
    int x = subgraph.getX();
    int z = subgraph.getZ();
    Map<EndpointPair<Vertex>, Float> toChange = new HashMap<>();
    if (subgraphGrid.contains(x, z)) return toChange;
    if (!correctWorld(subgraph)) throw new IllegalArgumentException("Subgraph world does not match Supergraph!");
    cloneGraphData(subgraph);
    subgraphGrid.put(x, z, subgraph);
    float maxFlow = subgraph.outEdges(subgraph.src).stream().map(Edge -> Edge.f).reduce(0f, (a, b) -> a + b);
    MaxFlow.createEdge(this, src, subgraph.src, Float.POSITIVE_INFINITY);
    MaxFlow.increaseFlow(this, src, subgraph.src, maxFlow);
    MaxFlow.createEdge(this, subgraph.dest, dest, Float.POSITIVE_INFINITY);
    MaxFlow.increaseFlow(this, subgraph.dest, dest, maxFlow);
    if (subgraphGrid.contains(x - 1, z)) toChange.putAll(stitchChunks(subgraphGrid.get(x - 1, z), subgraph));
    if (subgraphGrid.contains(x + 1, z)) toChange.putAll(stitchChunks(subgraph, subgraphGrid.get(x + 1, z)));
    if (subgraphGrid.contains(x, z - 1)) toChange.putAll(stitchChunks(subgraphGrid.get(x, z - 1), subgraph));
    if (subgraphGrid.contains(x, z + 1)) toChange.putAll(stitchChunks(subgraph, subgraphGrid.get(x, z + 1)));
    return toChange;
  }

  public void addAll(Collection<SubGraph> subgraphs) {
    Map<EndpointPair<Vertex>, Float> toChange = new HashMap<>();
    for (SubGraph subgraph : subgraphs) {
      toChange.putAll(addOne(subgraph));
    }
    if (toChange.isEmpty()) return;
    iteration++;
    MaxFlow.changeEdges(this, dists, src, dest, toChange);
    calculateState();
  }

  public void add(SubGraph subgraph) {
    Map<EndpointPair<Vertex>, Float> toChange = addOne(subgraph);
    if (toChange.isEmpty()) return;
    iteration++;
    MaxFlow.changeEdges(this, dists, src, dest, toChange);
    calculateState();
  }

  private void removeOne(SubGraph subgraph) {
    // beware adding again after removal
    int x = subgraph.getX();
    int z = subgraph.getZ();
    if (!subgraphGrid.contains(x, z)) return;
    if (subgraphGrid.contains(x - 1, z)) unravelChunks(subgraphGrid.get(x - 1, z), subgraph);
    if (subgraphGrid.contains(x + 1, z)) unravelChunks(subgraph, subgraphGrid.get(x + 1, z));
    if (subgraphGrid.contains(x, z - 1)) unravelChunks(subgraphGrid.get(x, z - 1), subgraph);
    if (subgraphGrid.contains(x, z + 1)) unravelChunks(subgraph, subgraphGrid.get(x, z + 1));
    subgraphGrid.remove(x, z);
    MaxFlow.removeEdge(this, src, subgraph.src);
    MaxFlow.removeEdge(this, subgraph.dest, dest);
  }

  public void removeAll() {
    // We dont have to worry about MaxFlow.changeEdges here :)
    for (SubGraph subgraph : new ArrayList<>(subgraphGrid.values())) {
      removeOne(subgraph);
    }
    // Empty state
    calculateState();
  }

  public void remove(SubGraph subgraph) {
    removeOne(subgraph);
    calculateState();
  }

  private boolean correctWorld(SubGraph subgraph) {
    return this.world == subgraph.getWorld();
  }

  public World getWorld() {
    return world;
  }

  public IntegerXZ getOriginXZ() {
    return chunkOrigin;
  }

  /**
   * Prepare for cross-chunk flows: <br>
   * 1. update edges between chunks with appropriate caps associated with snapshot material <br>
   * 2. translate caps from special cardinal nodes to corresponding flows and set caps to zero <br>
   * 3. record any violations i.Edge f > cap
   *
   * @param a
   * @param b
   * @return edges that need to change safely through the MaxFlow.changeEdges algo
   */
  private Map<EndpointPair<Vertex>, Float> stitchChunks(SubGraph a, SubGraph b) {
    int dx = b.getX() - a.getX();
    int dz = b.getZ() - a.getZ();
    assert (dx == 0 || dz == 0) && (dx * dx == 1 || dz * dz == 1);
    Cardinal direction = Cardinal.from(dx, dz);
    assert (direction == Cardinal.EAST || direction == Cardinal.SOUTH);
    Cardinal oppositeDirection = direction.opposite();
    Vertex aDest = new Vertex(a.getChunk(), ReservedID.destFrom(direction));
    Vertex aSource = new Vertex(a.getChunk(), ReservedID.sourceFrom(direction));
    Vertex bDest = new Vertex(b.getChunk(), ReservedID.destFrom(oppositeDirection));
    Vertex bSource = new Vertex(b.getChunk(), ReservedID.sourceFrom(oppositeDirection));
    Integrity AToB = Integrity.from(direction);
    Integrity BToA = Integrity.from(oppositeDirection);
    // Collect edge caps that need to be changed
    Map<EndpointPair<Vertex>, Float> toChange = new HashMap<>();
    for (int y = 0; y < 256; y++) {
      for (int xz = 0; xz < 16; xz++) {
        Block aBlock;
        Block bBlock;
        Material aMat;
        Material bMat;
        if (direction == Cardinal.SOUTH) {
          int x = xz;
          aBlock = a.getChunk().getBlock(x, y, 15);
          bBlock = b.getChunk().getBlock(x, y, 0);
          aMat = a.getSnapshot().getBlockType(x, y, 15);
          bMat = b.getSnapshot().getBlockType(x, y, 0);
        } else { // EAST
          int z = xz;
          aBlock = a.getChunk().getBlock(15, y, z);
          bBlock = b.getChunk().getBlock(0, y, z);
          aMat = a.getSnapshot().getBlockType(15, y, z);
          bMat = b.getSnapshot().getBlockType(0, y, z);
        }
        Vertex aVertex = new Vertex(aBlock);
        Vertex bVertex = new Vertex(bBlock);
        // reset cached flows
        Edge aSourceEdge = a.edgeConnectingOrNull(aSource, aVertex);
        Edge aDestEdge = a.edgeConnectingOrNull(aVertex, aDest);
        if (aSourceEdge.cap != 0) toChange.put(EndpointPair.ordered(aSource, aVertex), 0f);
        if (aDestEdge.cap != 0) toChange.put(EndpointPair.ordered(aVertex, aDest), 0f);
        Edge bSourceEdge = b.edgeConnectingOrNull(bSource, bVertex);
        Edge bDestEdge = b.edgeConnectingOrNull(bVertex, bDest);
        if (bSourceEdge.cap != 0) toChange.put(EndpointPair.ordered(bSource, bVertex), 0f);
        if (bDestEdge.cap != 0) toChange.put(EndpointPair.ordered(bVertex, bDest), 0f);
        // Update block edges out of chunk to the snapshot
        EndpointPair<Vertex> ab = EndpointPair.ordered(aVertex, bVertex);
        EndpointPair<Vertex> ba = EndpointPair.ordered(bVertex, aVertex);
        EnumMap<Integrity, Float> aStructure = Config.Instance().getStructuralData(aMat);
        EnumMap<Integrity, Float> bStructure = Config.Instance().getStructuralData(bMat);
        // Edges are nonexistant, so add with zero capacity
        MaxFlow.createEdge(this, aVertex, bVertex, 0f);
        toChange.put(ab, aStructure.get(AToB));
        toChange.put(ba, bStructure.get(BToA));
      }
    }
    return toChange;
  }

  private void unravelChunks(SubGraph a, SubGraph b) {
    int dx = b.getX() - a.getX();
    int dz = b.getZ() - a.getZ();
    assert (dx == 0 || dz == 0) && (dx * dx == 1 || dz * dz == 1);
    Cardinal direction = Cardinal.from(dx, dz);
    assert (direction == Cardinal.EAST || direction == Cardinal.SOUTH);
    Vertex aDest = new Vertex(a.getChunk(), ReservedID.destFrom(direction));
    Vertex aSource = new Vertex(a.getChunk(), ReservedID.sourceFrom(direction));
    Vertex bDest = new Vertex(b.getChunk(), ReservedID.destFrom(direction.opposite()));
    Vertex bSource = new Vertex(b.getChunk(), ReservedID.sourceFrom(direction.opposite()));

    for (int y = 0; y < 256; y++) {
      for (int xz = 0; xz < 16; xz++) {
        Block aBlock;
        Block bBlock;
        if (direction == Cardinal.SOUTH) {
          aBlock = a.getChunk().getBlock(xz, y, 15);
          bBlock = b.getChunk().getBlock(xz, y, 0);
        } else { // EAST
          aBlock = a.getChunk().getBlock(15, y, xz);
          bBlock = b.getChunk().getBlock(0, y, xz);
        }
        Vertex aVertex = new Vertex(aBlock);
        Vertex bVertex = new Vertex(bBlock);
        // These edges only live on supergraph
        Edge ab = this.edgeConnectingOrNull(aVertex, bVertex);
        Edge ba = this.edgeConnectingOrNull(bVertex, aVertex);
        assert ab.f == -ba.f : "Invalid backedge";
        // manual flow redirection :(
        if (ab.f > 0) {
          Edge bSourceEdge = b.edgeConnectingOrNull(bSource, bVertex);
          Edge aDestEdge = a.edgeConnectingOrNull(aVertex, aDest);
          float df = ab.f;
          bSourceEdge.cap = df;
          aDestEdge.cap = df;
          MaxFlow.increaseFlow(b, bSource, bVertex, df);
          MaxFlow.increaseFlow(a, aVertex, aDest, df);
          MaxFlow.increaseFlow(b, b.src, bSource, df);
          MaxFlow.increaseFlow(a, aDest, a.dest, df);
          MaxFlow.increaseFlow(this, src, b.src, df);
          MaxFlow.increaseFlow(this, a.dest, dest, df);
        } else if (ba.f > 0) {
          Edge aSourceEdge = a.edgeConnectingOrNull(aSource, aVertex);
          Edge bDestEdge = b.edgeConnectingOrNull(bVertex, bDest);
          float df = ba.f;
          aSourceEdge.cap = df;
          bDestEdge.cap = df;
          MaxFlow.increaseFlow(a, aSource, aVertex, df);
          MaxFlow.increaseFlow(b, bVertex, bDest, df);
          MaxFlow.increaseFlow(a, a.src, aSource, df);
          MaxFlow.increaseFlow(b, bDest, b.dest, df);
          MaxFlow.increaseFlow(this, src, a.src, df);
          MaxFlow.increaseFlow(this, b.dest, dest, df);
        }
        MaxFlow.removeEdge(this, aVertex, bVertex);
      }
    }
  }

  private void cloneGraphData(SubGraph subgraph) {
    // Beware data already existing (from after prior removal)
    for (Vertex node : subgraph.nodes()) {
      this.addNode(node);
    }
    for (Edge edge : subgraph.edges()) {
      EndpointPair<Vertex> endpointPair = subgraph.incidentNodes(edge);
      this.addEdge(endpointPair.nodeU(), endpointPair.nodeV(), edge);
    }
  }

  public List<Block> getIntegrityViolations() {
    List<Vertex> offending = this.state.offendingNodes;
    // Translate vertices to Blocks w/ Locations
    return offending.stream()
        .map(v -> v.getBlockXYZ())
        .filter(o -> o.isPresent())
        .map(
            o -> {
              IntegerXYZ xyz = o.get();
              // TODO: send BlockData instead to verify the block being broken
              return this.world.getBlockAt(xyz.x, xyz.y, xyz.z);
            })
        .collect(Collectors.toList());
  }

  private GraphState calculateState() {
    GraphState result = new GraphState();
    // Call to graph.nodes() preserves order
    for (SubGraph subgraph : getSubgraphGrid().values()) {
      for (Vertex v : subgraph.nodes()) {
        if (dists.getOrDefault(v, -1) == -1) continue;
        Optional<IntegerXYZ> xyzOpt = v.getBlockXYZ();
        // Only block-like vertices are relevant
        if (xyzOpt.isPresent()) {
          // Mark if the current group of offending blocks relies on another chunk
          IntegerXYZ xyz = xyzOpt.get();
          int x = xyz.x;
          int z = xyz.z;
          boolean isOffending = false;
          Edge e = subgraph.edgeConnectingOrNull(subgraph.src, v);
          if (e.cap > 0) {
            isOffending = true;
          }
          if (x % 16 == 0) { // WEST
            Edge e2 = subgraph.edgeConnectingOrNull(subgraph.west_src, v);
            if (e.cap > 0 || e2.cap > 0) {
              isOffending = true;
              result.dependantChunks.add(new IntegerXZ(Math.floorDiv(x - 1, 16), Math.floorDiv(z, 16)));
            }
          }
          if ((x + 1) % 16 == 0) { // EAST
            Edge e2 = subgraph.edgeConnectingOrNull(subgraph.east_src, v);
            if (e.cap > 0 || e2.cap > 0) {
              isOffending = true;
              result.dependantChunks.add(new IntegerXZ(Math.floorDiv(x + 1, 16), Math.floorDiv(z, 16)));
            }
          }
          if (z % 16 == 0) { // NORTH
            Edge e2 = subgraph.edgeConnectingOrNull(subgraph.north_src, v);
            if (e.cap > 0 || e2.cap > 0) {
              isOffending = true;
              result.dependantChunks.add(new IntegerXZ(Math.floorDiv(x, 16), Math.floorDiv(z - 1, 16)));
            }
          }
          if ((z + 1) % 16 == 0) { // SOUTH
            Edge e2 = subgraph.edgeConnectingOrNull(subgraph.south_src, v);
            if (e.cap > 0 || e2.cap > 0) {
              isOffending = true;
              result.dependantChunks.add(new IntegerXZ(Math.floorDiv(x, 16), Math.floorDiv(z + 1, 16)));
            }
          }
          if (isOffending) {
            result.offendingNodes.add(v);
          }
        }
      }
    }
    this.state = result;
    return this.state;
  }

  public GraphState getState() {
    if (this.state == null) this.state = new GraphState();
    return this.state;
  }

  public Table<Integer, Integer, SubGraph> getSubgraphGrid() {
    return subgraphGrid;
  }

  public int getIteration() {
    return iteration;
  }

  @Override
  protected MutableNetwork<Vertex, Edge> delegate() {
    return network;
  }

  @Override
  public String toString() {
    return String.format("SuperGraph(origin:%s, iter:%s)", chunkOrigin.toString(), iteration);
  }
}
