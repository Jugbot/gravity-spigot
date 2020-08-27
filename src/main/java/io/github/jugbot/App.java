package io.github.jugbot;

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
    ConfigurationSerialization.registerClass(BlockData.class);
    Config.Instance();
    ChunkProcessor.Instance();
    IntegrityChunkStorage.Instance();
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
