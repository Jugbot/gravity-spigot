package io.github.jugbot.graph;

import java.util.AbstractList;
import java.util.ArrayList;

/** Class for optimised Vertex with fixed (max) edge count */
public class FixedVertex<T> extends AbstractList<T> {
  private final T[] list;

  public FixedVertex(T[] list) {
    this.list = list;
  }

  @Override
  public T get(int index) {
    // TODO Auto-generated method stub
    return list[index];
  }

  @Override
  public int size() {
    // TODO Auto-generated method stub
    return list.length;
  }
}
