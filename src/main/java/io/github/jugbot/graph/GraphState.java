package io.github.jugbot.graph;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.github.jugbot.util.IntegerXZ;

public class GraphState {
  public List<Vertex> offendingNodes = new ArrayList<>();
  // public List<Vertex> dirtyNodes = new ArrayList<>();
  public Set<IntegerXZ> dependantChunks = new HashSet<>();
}
