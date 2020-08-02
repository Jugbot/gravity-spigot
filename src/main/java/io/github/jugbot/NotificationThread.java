package io.github.jugbot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.Callable;

public abstract class NotificationThread<T> implements Callable<T> {
  /**
   * An abstract function that children must implement. This function is where
   * all work - typically placed in the run of runnable - should be placed.
   */
  public abstract T doWork();

  /** 
   * Our list of listeners to be notified upon thread completion. 
   */
  private java.util.List<TaskListener<T>> listeners = Collections.synchronizedList(new ArrayList<TaskListener<T>>());

  /**
   * Adds a listener to this object. 
   * @param listener Adds a new listener to this object.
   */
  public void addListener(TaskListener<T> listener) {
    listeners.add(listener);
  }

  /**
   * Removes a particular listener from this object, or does nothing if the
   * listener is not registered. 
   * @param listener The listener to remove.
   */
  public void removeListener(TaskListener<T> listener) {
    listeners.remove(listener);
  }

  /**
   * Notifies all listeners that the thread has completed. 
   */
  private final void notifyListeners(T val) {
    synchronized (listeners) {
      for (TaskListener<T> listener : listeners) {
        listener.threadComplete(this, val);
      }
    }
  }

  /**
   * Implementation of the Runnable interface. This function first calls
   * doRun(), then notifies all listeners of completion.
   */
  public T call() {
    notifyListeners(doWork());
    return null;
  }
}