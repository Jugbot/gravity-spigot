package io.github.jugbot.graph;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.graph.MutableNetwork;
import com.google.common.graph.Network;
import com.google.common.graph.NetworkBuilder;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Sets;
import com.google.common.collect.Streams;
import com.google.common.collect.Table;
import com.google.common.graph.AbstractNetwork;
import com.google.common.graph.ElementOrder;
import com.google.common.graph.EndpointPair;
import com.google.common.graph.ImmutableNetwork;

public class SuperGraph extends AbstractNetwork<Vertex, Edge> implements MutableNetwork<Vertex, Edge> {
  // graphs stored by chunk coordinates
  private Table<Integer, Integer, SubGraph> subgraphs = HashBasedTable.create();
  // for misc temporary mutation methods
  private MutableNetwork<Vertex, Edge> transientSuperGraph = NetworkBuilder.directed().build();

  public boolean add(int x, int z, SubGraph subgraph) {
    if (subgraph.allowsParallelEdges() != this.allowsParallelEdges()
        || subgraph.allowsSelfLoops() != this.allowsSelfLoops()
        || subgraph.isDirected() != this.isDirected()) throw new IllegalArgumentException();
    if (subgraphs.contains(x, z)) return false;
    subgraphs.put(x, z, subgraph);
    return true;
  }

  public Optional<Network<Vertex, Edge>> get(int x, int z) {
    return Optional.ofNullable(subgraphs.get(x, z));
  }

  private Stream<Network<Vertex, Edge>> subGraphStream() {
    return Streams.<Network<Vertex, Edge>>concat(Stream.of(transientSuperGraph), subgraphs.values().stream());
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
