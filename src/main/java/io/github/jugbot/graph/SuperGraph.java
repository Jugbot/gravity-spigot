package io.github.jugbot.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.collect.Streams;
import com.google.common.collect.Table;
import com.google.common.graph.AbstractNetwork;
import com.google.common.graph.ElementOrder;
import com.google.common.graph.EndpointPair;
import com.google.common.graph.MutableNetwork;
import com.google.common.graph.Network;
import com.google.common.graph.NetworkBuilder;

import org.apache.commons.lang.NotImplementedException;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;

import io.github.jugbot.Config;
import io.github.jugbot.Integrity;
import io.github.jugbot.util.Cardinal;
import io.github.jugbot.util.IntegerXYZ;
import io.github.jugbot.util.IntegerXZ;

public class SuperGraph extends AbstractNetwork<Vertex, Edge> implements MutableNetwork<Vertex, Edge> {
  // graphs stored by chunk coordinates
  private Table<Integer, Integer, SubGraph> subgraphs = HashBasedTable.create();
  // for misc temporary mutation methods
  private MutableNetwork<Vertex, Edge> transientSuperGraph = NetworkBuilder.directed().build();
  public Map<Vertex, Integer> dists = new HashMap<>();
  private Vertex src = new Vertex(ReservedID.SUPER_SOURCE);
  private Vertex dest = new Vertex(ReservedID.SUPER_DEST);

  public boolean add(SubGraph subgraph) {
    int x = subgraph.getX();
    int z = subgraph.getZ();
    if (subgraphs.contains(x, z)) return false;
    subgraphs.put(x, z, subgraph);
    transientSuperGraph.addEdge(src, subgraph.src, new Edge(Float.POSITIVE_INFINITY));
    transientSuperGraph.addEdge(subgraph.dest, dest, new Edge(Float.POSITIVE_INFINITY));
    Map<EndpointPair<Vertex>, Float> toChange = new HashMap<>();
    if (subgraphs.contains(x - 1, z)) toChange.putAll(stitchChunks(subgraph, subgraphs.get(x - 1, z)));
    if (subgraphs.contains(x + 1, z)) toChange.putAll(stitchChunks(subgraph, subgraphs.get(x + 1, z)));
    if (subgraphs.contains(x, z - 1)) toChange.putAll(stitchChunks(subgraph, subgraphs.get(x, z - 1)));
    if (subgraphs.contains(x, z + 1)) toChange.putAll(stitchChunks(subgraph, subgraphs.get(x, z + 1)));
    MaxFlow.changeEdges(this, dists, src, dest, toChange);
    return true;
  }

  public boolean remove(SubGraph subgraph) {
    int x = subgraph.getX();
    int z = subgraph.getZ();
    if (subgraphs.contains(x, z)) return false;
    if (subgraphs.contains(x - 1, z)) unravelChunks(subgraph, subgraphs.get(x - 1, z));
    if (subgraphs.contains(x + 1, z)) unravelChunks(subgraph, subgraphs.get(x + 1, z));
    if (subgraphs.contains(x, z - 1)) unravelChunks(subgraph, subgraphs.get(x, z - 1));
    if (subgraphs.contains(x, z + 1)) unravelChunks(subgraph, subgraphs.get(x, z + 1));
    transientSuperGraph.removeEdge(transientSuperGraph.edgeConnectingOrNull(src, subgraph.src));
    transientSuperGraph.removeEdge(transientSuperGraph.edgeConnectingOrNull(subgraph.dest, dest));
    subgraphs.remove(x, z);
    return true;
  }

  public void empty() {}

  /**
   * Prepare for cross-chunk flows: <br>
   * 1. update edges between chunks with appropriate caps associated with snapshot material <br>
   * 2. translate caps from special cardinal nodes to corresponding flows and set caps to zero <br>
   * 3. record any violations i.e f > cap
   *
   * @param a
   * @param b
   * @return edges that need to change safely through the MaxFlow.changeEdges algo
   */
  private Map<EndpointPair<Vertex>, Float> stitchChunks(SubGraph a, SubGraph b) {
    int dx = a.getX() - b.getX();
    int dz = a.getZ() - b.getZ();
    assert (dx == 0 || dz == 0) && (dx * dx == 1 || dz * dz == 1);
    Cardinal direction = Cardinal.from(dx, dz);
    assert (direction == Cardinal.EAST || direction == Cardinal.SOUTH);
    Vertex aDest = new Vertex(ReservedID.destFrom(direction));
    Vertex aSource = new Vertex(ReservedID.sourceFrom(direction));
    Vertex bDest = new Vertex(ReservedID.destFrom(direction.opposite()));
    Vertex bSource = new Vertex(ReservedID.sourceFrom(direction.opposite()));
    Integrity AToB = Integrity.from(direction);
    Integrity BToA = Integrity.from(direction.opposite());
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
        // TODO: potential optimisation here if need be
        Edge aSourceEdge = a.edgeConnectingOrNull(aSource, aVertex);
        Edge aDestEdge = a.edgeConnectingOrNull(aVertex, aDest);
        if (aSourceEdge.cap != 0) toChange.put(EndpointPair.ordered(aSource, aVertex), 0f);
        if (aDestEdge.cap != 0) toChange.put(EndpointPair.ordered(aVertex, aDest), 0f);
        Edge bSourceEdge = b.edgeConnectingOrNull(bSource, bVertex);
        Edge bDestEdge = b.edgeConnectingOrNull(bVertex, bDest);
        if (bSourceEdge.cap != 0) toChange.put(EndpointPair.ordered(bSource, bVertex), 0f);
        if (bDestEdge.cap != 0) toChange.put(EndpointPair.ordered(bVertex, bDest), 0f);
        // Update block edges out of chunk to the snapshot
        // Technically this only has to be done for b->a edges but wth
        EndpointPair<Vertex> ab = EndpointPair.ordered(aVertex, bVertex);
        EndpointPair<Vertex> ba = EndpointPair.ordered(bVertex, aVertex);
        EnumMap<Integrity, Float> aStructure = Config.getStructuralData(aMat);
        EnumMap<Integrity, Float> bStructure = Config.getStructuralData(bMat);
        toChange.put(ab, aStructure.get(AToB));
        toChange.put(ba, bStructure.get(BToA));
      }
    }
    return toChange;
  }

  private void increaseFlow(SubGraph graph, Vertex a, Vertex b, float df) {
    graph.edgeConnectingOrNull(a, b).f += df;
    graph.edgeConnectingOrNull(b, a).f -= df;
  }

  private void unravelChunks(SubGraph a, SubGraph b) {
    int dx = a.getX() - b.getX();
    int dz = a.getZ() - b.getZ();
    assert (dx == 0 || dz == 0) && (dx * dx == 1 || dz * dz == 1);
    Cardinal direction = Cardinal.from(dx, dz);
    assert (direction == Cardinal.EAST || direction == Cardinal.SOUTH);
    Vertex aDest = new Vertex(ReservedID.destFrom(direction));
    Vertex aSource = new Vertex(ReservedID.sourceFrom(direction));
    Vertex bDest = new Vertex(ReservedID.destFrom(direction.opposite()));
    Vertex bSource = new Vertex(ReservedID.sourceFrom(direction.opposite()));

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
        // A graph should own both these edges (not a typo)
        Edge ab = a.edgeConnectingOrNull(aVertex, bVertex);
        Edge ba = a.edgeConnectingOrNull(bVertex, aVertex);
        assert ab.f == -ba.f : "Invalid backedge";
        // manual flow redirection :(
        if (ab.f > 0) {
          Edge bSourceEdge = b.edgeConnectingOrNull(bSource, bVertex);
          Edge aDestEdge = a.edgeConnectingOrNull(aVertex, aDest);
          float df = ab.f;
          bSourceEdge.cap = df;
          aDestEdge.cap = df;
          increaseFlow(b, bSource, bVertex, df);
          increaseFlow(a, aVertex, aDest, df);
          increaseFlow(b, b.src, bSource, df);
          increaseFlow(a, aDest, a.dest, df);
        } else if (ba.f > 0) {
          Edge aSourceEdge = a.edgeConnectingOrNull(aSource, aVertex);
          Edge bDestEdge = b.edgeConnectingOrNull(bVertex, bDest);
          float df = ba.f;
          aSourceEdge.cap = df;
          bDestEdge.cap = df;
          increaseFlow(a, aSource, aVertex, df);
          increaseFlow(b, bVertex, bDest, df);
          increaseFlow(a, a.src, aSource, df);
          increaseFlow(b, bDest, b.dest, df);
        } else {
          // edge flows are zero
          continue;
        }
        ab.f = 0;
        ba.f = 0;
      }
    }
  }

  public Optional<Network<Vertex, Edge>> get(int x, int z) {
    return Optional.ofNullable(subgraphs.get(x, z));
  }

  private Stream<Network<Vertex, Edge>> subGraphStream() {
    System.out.println("Warning: dumb graph search");
    return Streams.<Network<Vertex, Edge>>concat(Stream.of(transientSuperGraph), subgraphs.values().stream());
  }

  // Shortcuts the dumb search of subGraphStream()
  private List<Network<Vertex, Edge>> relevantGraphs(Vertex v) {
    Optional<IntegerXZ> pos = v.getChunkXZ();
    if (!pos.isPresent()) return Lists.newArrayList(transientSuperGraph);
    List<Network<Vertex, Edge>> result = new ArrayList<>();
    result.add(subgraphs.get(pos.get().x, pos.get().z));
    // Also get adjacent chunk if bordering
    Optional<IntegerXYZ> pos2 = v.getBlockXYZ();
    if (pos2.isPresent()) {
      int x = pos2.get().x;
      int z = pos2.get().z;
      // TODO
    }
    throw new NotImplementedException();
  }

  @Override
  public Set<Vertex> nodes() {
    // Note identically hashed objects are picked at random
    return subGraphStream()
        .map(net -> net.nodes())
        .flatMap(set -> set.stream())
        .collect(Collectors.toCollection(HashSet::new));
  }

  @Override
  public Set<Edge> edges() {
    // Note identically hashed objects are picked at random
    return subGraphStream()
        .map(net -> net.edges())
        .flatMap(set -> set.stream())
        .collect(Collectors.toCollection(HashSet::new));
  }

  @Override
  public boolean isDirected() {
    return true;
  }

  @Override
  public boolean allowsParallelEdges() {
    return false;
  }

  @Override
  public boolean allowsSelfLoops() {
    return false;
  }

  @Override
  public ElementOrder<Vertex> nodeOrder() {
    return ElementOrder.insertion();
  }

  @Override
  public ElementOrder<Edge> edgeOrder() {
    return ElementOrder.unordered();
  }

  @Override
  public Set<Vertex> adjacentNodes(Vertex node) {
    // TODO: throw if Vertex DNE
    return subGraphStream()
        .filter(net -> net.nodes().contains(node))
        .map(net -> net.adjacentNodes(node))
        .flatMap(set -> set.stream())
        .collect(Collectors.toCollection(HashSet::new));
  }

  @Override
  public Set<Vertex> predecessors(Vertex node) {
    // TODO: throw if Vertex DNE
    return subGraphStream()
        .filter(net -> net.nodes().contains(node))
        .map(net -> net.predecessors(node))
        .flatMap(set -> set.stream())
        .collect(Collectors.toCollection(HashSet::new));
  }

  @Override
  public Set<Vertex> successors(Vertex node) {
    // TODO: throw if Vertex DNE
    return subGraphStream()
        .filter(net -> net.nodes().contains(node))
        .map(net -> net.successors(node))
        .flatMap(set -> set.stream())
        .collect(Collectors.toCollection(HashSet::new));
  }

  @Override
  public Set<Edge> incidentEdges(Vertex node) {
    // TODO: throw if Vertex DNE
    return subGraphStream()
        .filter(net -> net.nodes().contains(node))
        .map(net -> net.incidentEdges(node))
        .flatMap(set -> set.stream())
        .collect(Collectors.toCollection(HashSet::new));
  }

  @Override
  public Set<Edge> inEdges(Vertex node) {
    // TODO: throw if Vertex DNE
    return subGraphStream()
        .filter(net -> net.nodes().contains(node))
        .map(net -> net.inEdges(node))
        .flatMap(set -> set.stream())
        .collect(Collectors.toCollection(HashSet::new));
  }

  @Override
  public Set<Edge> outEdges(Vertex node) {
    // TODO: throw if Vertex DNE
    return subGraphStream()
        .filter(net -> net.nodes().contains(node))
        .map(net -> net.outEdges(node))
        .flatMap(set -> set.stream())
        .collect(Collectors.toCollection(HashSet::new));
  }

  @Override
  public EndpointPair<Vertex> incidentNodes(Edge edge) {
    Optional<EndpointPair<Vertex>> optional =
        subGraphStream().filter(net -> net.edges().contains(edge)).map(net -> net.incidentNodes(edge)).findAny();
    if (!optional.isPresent()) throw new IllegalArgumentException();
    return optional.get();
  }

  @Override
  public boolean addNode(Vertex node) {
    return transientSuperGraph.addNode(node);
  }

  @Override
  public boolean addEdge(Vertex nodeU, Vertex nodeV, Edge edge) {
    // TODO: throw if Edge exists
    return transientSuperGraph.addEdge(nodeU, nodeV, edge);
  }

  @Override
  public boolean addEdge(EndpointPair<Vertex> endpoints, Edge edge) {
    // TODO: throw if Edge exists
    return transientSuperGraph.addEdge(endpoints, edge);
  }

  @Override
  public boolean removeNode(Vertex node) {
    return transientSuperGraph.removeNode(node);
  }

  @Override
  public boolean removeEdge(Edge edge) {
    return transientSuperGraph.removeEdge(edge);
  }
}
