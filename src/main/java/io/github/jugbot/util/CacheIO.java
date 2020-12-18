package io.github.jugbot.util;

import java.util.HashMap;
import java.util.Map;

import com.google.common.cache.CacheLoader;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;

abstract class CacheLoaderAdapter<L, R> extends CacheLoader<L, R> {
  @Override
  public R load(L key) throws Exception {
    return onAdd(key);
  }

  public abstract R onAdd(L key) throws Exception;
}

public abstract class CacheIO<L, R> extends CacheLoader<L, R> implements RemovalListener<L, R> {
  // Keep loaded on eviction for saving to disk
  private Map<L, R> loaded = new HashMap<>();

  @Override
  public void onRemoval(RemovalNotification<L, R> notification) {
    this.write(notification.getKey(), notification.getValue());
    loaded.remove(notification.getKey(), notification.getValue());
  }

  @Override
  public R load(L key) throws Exception {
    R value = read(key);
    if (loaded.put(key, value) != null) throw new Exception();
    return value;
  }

  public abstract void write(L key, R subGraph);

  public abstract R read(L key);
}
