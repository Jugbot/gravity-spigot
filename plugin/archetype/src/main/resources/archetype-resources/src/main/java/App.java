#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package};

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.SimpleFormatter;
import org.bukkit.plugin.java.JavaPlugin;

/** Hello world! */
public class App extends JavaPlugin {
  private static App instance;

  public static App getInstance() {
    if (instance == null) {
      instance = App.getPlugin(App.class);
    }
    return instance;
  }

  @Override
  public void onLoad() {}

  @Override
  public void onEnable() {
    /** LOGGING */
    Handler handler;
    try {
      File logFile = new File(getDataFolder(), "logs" + File.separator + "latest.log");
      if (!logFile.getParentFile().exists()) {
        logFile.getParentFile().mkdirs();
      } else {
        // Clean directory
        for (File file : logFile.getParentFile().listFiles()) {
          file.delete();
        }
      }
      handler = new FileHandler(logFile.getAbsolutePath());
      handler.setFormatter(new SimpleFormatter());
      handler.setLevel(Level.ALL);
      getLogger().addHandler(handler);
    } catch (SecurityException | IOException e) {
      e.printStackTrace();
    }
    /** INIT */

    /** REGISTER */
    getLogger().info("Catchphrase!");
  }

  @Override
  public void onDisable() {
    getLogger().info("Goodbye...");
  }
}
