package io.github.jugbot.graph;

import java.io.Serializable;

public class Edge implements Serializable {
    public int t;
    public int rev;
    public float cap;
    public float f;

    public Edge(int t, int rev, float cap) {
      this.t = t;
      this.rev = rev;
      this.cap = cap;
    }
  }