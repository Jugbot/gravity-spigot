package io.github.jugbot.gravity.util;

import java.util.Comparator;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;

public class PriorityQueueSet<T> extends PriorityQueue<T> {
  Set<T> members = new HashSet<>();

  public PriorityQueueSet(Comparator<T> compare) {
    super(compare);
  }

  @Override
  public boolean add(T e) {
    if (members.contains(e)) return false;
    members.add(e);
    return super.add(e);
  }

  @Override
  public T remove() {
    members.remove(super.peek());
    return super.remove();
  }
}
