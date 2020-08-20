package io.github.jugbot.util;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.bukkit.Bukkit;

import io.github.jugbot.App;

public class AsyncBukkit {
  /**
   * Utility method for truly asynchronous tasks. CPU load should generally be heavy in task and light in callback.
   *
   * @param <T> The respective return and argument types of task and callback functions
   * @param task The function that does work async with the server
   * @param callback The function that consumes the result synchronously with the server
   */
  public static <T> void doTask(Supplier<T> task, Consumer<T> callback) {
    Bukkit.getScheduler()
        .runTaskAsynchronously(
            App.Instance(),
            new Runnable() {
              @Override
              public void run() {
                // NOTE: potential thread danger
                final T result = task.get();
                // go back to the tick loop
                Bukkit.getScheduler()
                    .runTask(
                        App.Instance(),
                        new Runnable() {
                          @Override
                          public void run() {
                            // call the callback with the result
                            callback.accept(result);
                          }
                        });
              }
            });
  }
}
