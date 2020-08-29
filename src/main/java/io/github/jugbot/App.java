package io.github.jugbot;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.SimpleFormatter;

import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.java.JavaPlugin;

import io.github.jugbot.commands.GravityCommand;

/** Hello world! */
public class App extends JavaPlugin {
  private static App instance;

  public static App Instance() {
    if (instance == null) {
      instance = App.getPlugin(App.class);
    }
    return instance;
  }

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
    ConfigurationSerialization.registerClass(BlockData.class);
    Config.Instance();
    ChunkProcessor.Instance();
    IntegrityChunkStorage.Instance();
    /** REGISTER */
    getServer().getPluginManager().registerEvents(new ChunkListener(), this);
    getServer().getPluginManager().registerEvents(new BlockEventListeners(), this);
    getServer().getPluginManager().registerEvents(new BlockGravityListener(), this);
    getServer().getPluginManager().registerEvents(new BlockChangeListener(), this);
    getCommand("gr").setExecutor(new GravityCommand());
    // ProtocolManager manager = ProtocolLibrary.getProtocolManager();
    // manager.addPacketListener(new BlockChanger());
    getLogger().info("God turned on gravity!");
  }

  @Override
  public void onDisable() {
    getLogger().info("Gravity shutting down...");
  }
}
