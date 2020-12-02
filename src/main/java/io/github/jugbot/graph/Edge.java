package io.github.jugbot.graph;

public class Edge {
  public float cap;
  public float f = 0;

  public Edge(float cap) {
    this.cap = cap;
  }

  @Override
  public String toString() {
    return String.format("Edge(%f / %f)", f, cap);
  }
}
