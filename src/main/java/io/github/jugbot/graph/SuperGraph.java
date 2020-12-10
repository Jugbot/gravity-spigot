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
import com.google.common.graph.MutableNetwork;
import com.google.common.graph.Network;
import com.google.common.graph.NetworkBuilder;
import com.google.common.math.IntMath;

import org.apache.commons.lang.NotImplementedException;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;

import io.github.jugbot.Asserts;
import io.github.jugbot.Config;
import io.github.jugbot.Integrity;
import io.github.jugbot.util.Cardinal;
import io.github.jugbot.util.IntegerXYZ;
import io.github.jugbot.util.IntegerXZ;

public class SuperGraph extends AbstractNetwork<Vertex, Edge> implements MutableNetwork<Vertex, Edge> {
  // graphs stored by chunk coordinates
  private Table<Integer, Integer, SubGraph> subgraphs = HashBasedTable.create();
  // for misc temporary mutation methods
  private MutableNetwork<Vertex, Edge> transientSubGraph = NetworkBuilder.directed().build();
  public Map<Vertex, Integer> dists = new HashMap<>();
  private Vertex src = new Vertex(ReservedID.SUPER_SOURCE);
  private Vertex dest = new Vertex(ReservedID.SUPER_DEST);

  public boolean add(SubGraph subgraph) {
    int x = subgraph.getX();
    int z = subgraph.getZ();
    if (subgraphs.contains(x, z)) return false;
    subgraphs.put(x, z, subgraph);
    // TODO: fix flow on these two edges
    float maxFlow = subgraph.outEdges(subgraph.src).stream().map(Edge -> Edge.f).reduce(0f, (a, b) -> a + b);
    MaxFlow.createEdge(transientSubGraph, src, subgraph.src, Float.POSITIVE_INFINITY);
    MaxFlow.increaseFlow(transientSubGraph, src, subgraph.src, maxFlow);
    MaxFlow.createEdge(transientSubGraph, subgraph.dest, dest, Float.POSITIVE_INFINITY);
    MaxFlow.increaseFlow(transientSubGraph, subgraph.dest, dest, maxFlow);
    Map<EndpointPair<Vertex>, Float> toChange = new HashMap<>();
    if (subgraphs.contains(x - 1, z)) toChange.putAll(stitchChunks(subgraph, subgraphs.get(x - 1, z)));
    if (subgraphs.contains(x + 1, z)) toChange.putAll(stitchChunks(subgraph, subgraphs.get(x + 1, z)));
    if (subgraphs.contains(x, z - 1)) toChange.putAll(stitchChunks(subgraph, subgraphs.get(x, z - 1)));
    if (subgraphs.contains(x, z + 1)) toChange.putAll(stitchChunks(subgraph, subgraphs.get(x, z + 1)));
    // MaxFlow.changeEdges(this, dists, src, dest, toChange);
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
    MaxFlow.removeEdge(transientSubGraph, src, subgraph.src);
    MaxFlow.removeEdge(transientSubGraph, subgraph.dest, dest);
    subgraphs.remove(x, z);
    return true;
  }

  public void empty() {}

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
          MaxFlow.increaseFlow(b, bSource, bVertex, df);
          MaxFlow.increaseFlow(a, aVertex, aDest, df);
          MaxFlow.increaseFlow(b, b.src, bSource, df);
          MaxFlow.increaseFlow(a, aDest, a.dest, df);
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
    // System.out.println("Warning: dumb graph search");
    return Streams.<Network<Vertex, Edge>>concat(Stream.of(transientSubGraph), subgraphs.values().stream());
  }

  // Shortcuts the dumb search of subGraphStream()
  private List<Network<Vertex, Edge>> relevantGraphs(Vertex v) {
    List<Network<Vertex, Edge>> result = new ArrayList<>();
    // all vertices can be in the transient subgraph
    result.add(transientSubGraph);
    Optional<IntegerXZ> chunkPos = v.getChunkXZ();
    if (!chunkPos.isPresent()) return result;
    // add chunk containing vertex
    result.add(subgraphs.get(chunkPos.get().x, chunkPos.get().z));
    Optional<IntegerXYZ> blockPos = v.getBlockXYZ();
    if (!blockPos.isPresent()) return result;
    // add chunk bordering vertex
    int blockX = blockPos.get().x % 16;
    int blockZ = blockPos.get().z % 16;
    if (blockX == 0 || blockX == 15){
      Network<Vertex, Edge> subgraph = subgraphs.get(chunkPos.get().x + (blockX == 0 ?  -1 : 1), chunkPos.get().z);
      if (subgraph != null) result.add(subgraph);
    }
    if (blockZ == 0 || blockZ == 15){
      Network<Vertex, Edge> subgraph = subgraphs.get(chunkPos.get().x, chunkPos.get().z + (blockZ == 0 ?  -1 : 1));
      if (subgraph != null) result.add(subgraph);
    }
    return result;   
  }

  @Override
  public Set<Vertex> nodes() {
    // Note identically hashed objects are picked at random
    return subGraphStream()
        .map(net -> net.nodes())
        .flatMap(Set::stream)
        .collect(Collectors.toSet());
  }

  @Override
  public Set<Edge> edges() {
    // Note identically hashed objects are picked at random
    return subGraphStream()
        .map(net -> net.edges())
        .flatMap(Set::stream)
        .collect(Collectors.toSet());
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
        .flatMap(Set::stream)
        .collect(Collectors.toSet());
  }

  @Override
  public Set<Vertex> predecessors(Vertex node) {
    // TODO: throw if Vertex DNE
    return subGraphStream()
        .filter(net -> net.nodes().contains(node))
        .map(net -> net.predecessors(node))
        .flatMap(Set::stream)
        .collect(Collectors.toSet());
  }

  @Override
  public Set<Vertex> successors(Vertex node) {
    // TODO
    // TODO: throw if Vertex DNE
    return subGraphStream()
        .filter(net -> net.nodes().contains(node))
        .map(net -> net.successors(node))
        .flatMap(Set::stream)
        .collect(Collectors.toSet());
  }

  @Override
  public Set<Edge> incidentEdges(Vertex node) {
    // TODO: throw if Vertex DNE
    return subGraphStream()
        .filter(net -> net.nodes().contains(node))
        .map(net -> net.incidentEdges(node))
        .flatMap(Set::stream)
        .collect(Collectors.toSet());
  }

  @Override
  public Set<Edge> inEdges(Vertex node) {
    // TODO: throw if Vertex DNE
    return subGraphStream()
        .filter(net -> net.nodes().contains(node))
        .map(net -> net.inEdges(node))
        .flatMap(Set::stream)
        .collect(Collectors.toSet());
  }

  @Override
  public Set<Edge> outEdges(Vertex node) {
    // TODO: throw if Vertex DNE
    return subGraphStream()
        .filter(net -> net.nodes().contains(node))
        .map(net -> net.outEdges(node))
        .flatMap(Set::stream)
        .collect(Collectors.toSet());
  }

  @Override
  public EndpointPair<Vertex> incidentNodes(Edge edge) {
    Optional<EndpointPair<Vertex>> optional =
        subGraphStream()
        .filter(net -> net.edges().contains(edge))
        .map(net -> net.incidentNodes(edge))
        .findAny();
    if (!optional.isPresent()) throw new IllegalArgumentException();
    return optional.get();
  }

  @Override
  public boolean addNode(Vertex node) {
    return transientSubGraph.addNode(node);
  }

  @Override
  public boolean addEdge(Vertex nodeU, Vertex nodeV, Edge edge) {
    // TODO: throw if Edge exists
    return transientSubGraph.addEdge(nodeU, nodeV, edge);
  }

  @Override
  public boolean addEdge(EndpointPair<Vertex> endpoints, Edge edge) {
    // TODO: throw if Edge exists
    return transientSubGraph.addEdge(endpoints, edge);
  }

  @Override
  public boolean removeNode(Vertex node) {
    return transientSubGraph.removeNode(node);
  }

  @Override
  public boolean removeEdge(Edge edge) {
    return transientSubGraph.removeEdge(edge);
  }

  /**
   * AbstractGraph Overrides
   */

  /*
  @Override
  public int degree(Vertex node) {
    if (isDirected()) {
      return IntMath.saturatedAdd(inEdges(node).size(), outEdges(node).size());
    } else {
      return IntMath.saturatedAdd(incidentEdges(node).size(), edgesConnecting(node, node).size());
    }
  }

  @Override
  public int inDegree(Vertex node) {
    return isDirected() ? inEdges(node).size() : degree(node);
  }

  @Override
  public int outDegree(Vertex node) {
    return isDirected() ? outEdges(node).size() : degree(node);
  }

  @Override
  public Set<Edge> adjacentEdges(Edge edge) {
    EndpointPair<Vertex> endpointPair = incidentNodes(edge); // Verifies that edge is in this network.
    Set<Edge> endpointPairIncidentEdges =
        Sets.union(incidentEdges(endpointPair.nodeU()), incidentEdges(endpointPair.nodeV()));
    return Sets.difference(endpointPairIncidentEdges, ImmutableSet.of(edge));
  }

  @Override
  public Set<Edge> edgesConnecting(Vertex nodeU, Vertex nodeV) {
    Set<Edge> outEdgesU = outEdges(nodeU);
    Set<Edge> inEdgesV = inEdges(nodeV);
    return outEdgesU.size() <= inEdgesV.size()
        ? unmodifiableSet(Sets.filter(outEdgesU, connectedPredicate(nodeU, nodeV)))
        : unmodifiableSet(Sets.filter(inEdgesV, connectedPredicate(nodeV, nodeU)));
  }

  @Override
  public Set<Edge> edgesConnecting(EndpointPair<Vertex> endpoints) {
    validateEndpoints(endpoints);
    return edgesConnecting(endpoints.nodeU(), endpoints.nodeV());
  }

  private Predicate<Edge> connectedPredicate(final Vertex nodePresent, final Vertex nodeToCheck) {
    return new Predicate<Edge>() {
      @Override
      public boolean apply(Edge edge) {
        return incidentNodes(edge).adjacentNode(nodePresent).equals(nodeToCheck);
      }
    };
  }

  @Override
  public Optional<Edge> edgeConnecting(Vertex nodeU, Vertex nodeV) {
    return Optional.ofNullable(edgeConnectingOrNull(nodeU, nodeV));
  }

  @Override
  public Optional<Edge> edgeConnecting(EndpointPair<Vertex> endpoints) {
    validateEndpoints(endpoints);
    return edgeConnecting(endpoints.nodeU(), endpoints.nodeV());
  }

  @Override
  public @Nullable Edge edgeConnectingOrNull(Vertex nodeU, Vertex nodeV) {
    Set<Edge> edgesConnecting = edgesConnecting(nodeU, nodeV);
    switch (edgesConnecting.size()) {
      case 0:
        return null;
      case 1:
        return edgesConnecting.iterator().next();
      default:
        throw new IllegalArgumentException(String.format(MULTIPLE_EDGES_CONNECTING, nodeU, nodeV));
    }
  }

  @Override
  public @Nullable Edge edgeConnectingOrNull(EndpointPair<Vertex> endpoints) {
    validateEndpoints(endpoints);
    return edgeConnectingOrNull(endpoints.nodeU(), endpoints.nodeV());
  }

  @Override
  public boolean hasEdgeConnecting(Vertex nodeU, Vertex nodeV) {
    checkNotNull(nodeU);
    checkNotNull(nodeV);
    return nodes().contains(nodeU) && successors(nodeU).contains(nodeV);
  }

  @Override
  public boolean hasEdgeConnecting(EndpointPair<Vertex> endpoints) {
    checkNotNull(endpoints);
    if (!isOrderingCompatible(endpoints)) {
      return false;
    }
    return hasEdgeConnecting(endpoints.nodeU(), endpoints.nodeV());
  }

  protected final void validateEndpoints(EndpointPair<?> endpoints) {
    checkNotNull(endpoints);
    checkArgument(isOrderingCompatible(endpoints), ENDPOINTS_MISMATCH);
  }

  protected final boolean isOrderingCompatible(EndpointPair<?> endpoints) {
    return endpoints.isOrdered() || !this.isDirected();
  }

  @Override
  public String toString() {
    return "isDirected: "
        + isDirected()
        + ", allowsParallelEdges: "
        + allowsParallelEdges()
        + ", allowsSelfLoops: "
        + allowsSelfLoops()
        + ", nodes: "
        + nodes()
        + ", edges: "
        + edgeIncidentNodesMap(this);
  }

  private static Map<Edge, EndpointPair<Vertex>> edgeIncidentNodesMap(final Network<Vertex, Edge> network) {
    Function<Edge, EndpointPair<Vertex>> edgeToIncidentNodesFn =
        new Function<Edge, EndpointPair<Vertex>>() {
          @Override
          public EndpointPair<Vertex> apply(Edge edge) {
            return network.incidentNodes(edge);
          }
        };
    return Maps.asMap(network.edges(), edgeToIncidentNodesFn);
  }
  */
}
