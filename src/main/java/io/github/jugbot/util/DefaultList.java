package io.github.jugbot.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.AbstractList;
import java.util.ListIterator;

import org.omg.IOP.TAG_ALTERNATE_IIOP_ADDRESS;

public class DefaultList<T> extends AbstractList<T> implements Serializable {
  private T[] list;
  private final T fallback;
  
  public DefaultList(T[] list, T fallback) {
    this.fallback = fallback;
    this.list = list;
  }

  @Override
  public int size() {
    return list.length;
  }

  @Override
  public Iterator<T> iterator() {
    return Arrays.asList(list).iterator();
  }

  @Override
  public Object[] toArray() {
    return list;
  }

  @Override
  public T get(int index) {
    if (index >= list.length || index < 0) return fallback;
    if (list[index] == null) return fallback;
    return list[index];
  }

  @Override
  public T set(int index, T element) {
    return list[index] = element;
  }

  @Override
  public ListIterator<T> listIterator() {
    return Arrays.asList(list).listIterator();
  }

  @Override
  public ListIterator<T> listIterator(int index) {
    return Arrays.asList(list).listIterator(index);
  }

  @Override
  public List<T> subList(int fromIndex, int toIndex) {
    return Arrays.asList(list).subList(fromIndex, toIndex);
  }

}
