package io.github.jugbot.graph;

import java.util.Optional;
import java.util.Set;

import com.google.common.graph.AbstractNetwork;
import com.google.common.graph.ElementOrder;
import com.google.common.graph.EndpointPair;
import com.google.common.graph.Graph;
import com.google.common.graph.MutableNetwork;
import com.google.common.graph.Network;

abstract class ForwardingMutableNetwork<N, E> implements MutableNetwork<N, E> {

  protected abstract MutableNetwork<N, E> delegate();

  @Override
  public Set<N> nodes() {
    return delegate().nodes();
  }

  @Override
  public Set<E> edges() {
    return delegate().edges();
  }

  @Override
  public boolean isDirected() {
    return delegate().isDirected();
  }

  @Override
  public boolean allowsParallelEdges() {
    return delegate().allowsParallelEdges();
  }

  @Override
  public boolean allowsSelfLoops() {
    return delegate().allowsSelfLoops();
  }

  @Override
  public ElementOrder<N> nodeOrder() {
    return delegate().nodeOrder();
  }

  @Override
  public ElementOrder<E> edgeOrder() {
    return delegate().edgeOrder();
  }

  @Override
  public Set<N> adjacentNodes(N node) {
    return delegate().adjacentNodes(node);
  }

  @Override
  public Set<N> predecessors(N node) {
    return delegate().predecessors(node);
  }

  @Override
  public Set<N> successors(N node) {
    return delegate().successors(node);
  }

  @Override
  public Set<E> incidentEdges(N node) {
    return delegate().incidentEdges(node);
  }

  @Override
  public Set<E> inEdges(N node) {
    return delegate().inEdges(node);
  }

  @Override
  public Set<E> outEdges(N node) {
    return delegate().outEdges(node);
  }

  @Override
  public EndpointPair<N> incidentNodes(E edge) {
    return delegate().incidentNodes(edge);
  }

  @Override
  public Set<E> adjacentEdges(E edge) {
    return delegate().adjacentEdges(edge);
  }

  @Override
  public int degree(N node) {
    return delegate().degree(node);
  }

  @Override
  public int inDegree(N node) {
    return delegate().inDegree(node);
  }

  @Override
  public int outDegree(N node) {
    return delegate().outDegree(node);
  }

  @Override
  public Set<E> edgesConnecting(N nodeU, N nodeV) {
    return delegate().edgesConnecting(nodeU, nodeV);
  }

  @Override
  public Set<E> edgesConnecting(EndpointPair<N> endpoints) {
    return delegate().edgesConnecting(endpoints);
  }

  @Override
  public Optional<E> edgeConnecting(N nodeU, N nodeV) {
    return delegate().edgeConnecting(nodeU, nodeV);
  }

  @Override
  public Optional<E> edgeConnecting(EndpointPair<N> endpoints) {
    return delegate().edgeConnecting(endpoints);
  }

  @Override
  public E edgeConnectingOrNull(N nodeU, N nodeV) {
    return delegate().edgeConnectingOrNull(nodeU, nodeV);
  }

  @Override
  public E edgeConnectingOrNull(EndpointPair<N> endpoints) {
    return delegate().edgeConnectingOrNull(endpoints);
  }

  @Override
  public boolean hasEdgeConnecting(N nodeU, N nodeV) {
    return delegate().hasEdgeConnecting(nodeU, nodeV);
  }

  @Override
  public boolean hasEdgeConnecting(EndpointPair<N> endpoints) {
    return delegate().hasEdgeConnecting(endpoints);
  }

  @Override
  public Graph<N> asGraph() {
    return delegate().asGraph();
  }

  @Override
  public boolean addNode(N node) {
    return delegate().addNode(node);
  }

  @Override
  public boolean addEdge(N nodeU, N nodeV, E edge) {
    return delegate().addEdge(nodeU, nodeV, edge);
  }

  @Override
  public boolean addEdge(EndpointPair<N> endpoints, E edge) {
    return delegate().addEdge(endpoints, edge);
  }

  @Override
  public boolean removeNode(N node) {
    return delegate().removeNode(node);
  }

  @Override
  public boolean removeEdge(E edge) {
    return delegate().removeEdge(edge);
  }
}
