package io.github.jugbot.graph;

import java.io.Serializable;

public class Edge {
  public float cap;
  public float f = 0;

  public Edge(float cap) {
    this.cap = cap;
  }
}