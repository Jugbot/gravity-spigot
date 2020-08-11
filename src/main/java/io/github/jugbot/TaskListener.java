package io.github.jugbot;

import java.util.concurrent.Callable;

public interface TaskListener<T> {
  public void threadComplete(Callable<T> runner, T val);
}
