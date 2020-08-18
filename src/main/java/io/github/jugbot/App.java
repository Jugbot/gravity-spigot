package io.github.jugbot;

import java.lang.reflect.Type;
import java.util.Set;

import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.plugin.IllegalPluginAccessException;
import org.bukkit.plugin.java.JavaPlugin;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

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
    Reflections reflections =
        new Reflections(
            new ConfigurationBuilder()
                .setScanners(new SubTypesScanner(false))
                .setUrls(ClasspathHelper.forPackage("org.bukkit.event.block")));
    Set<Class<? extends BlockEvent>> classes = reflections.getSubTypesOf(BlockEvent.class);
    for (Class<? extends BlockEvent> clazz : classes) {
      try {
        getServer().getPluginManager().registerEvents(BlockListener.getBlockEventListener(clazz), this);
      } catch (IllegalPluginAccessException e) {
        System.out.println(e);
        // System.out.println("Could not listen to " + clazz.getCanonicalName());
      }
    }
    // getServer().getPluginManager().registerEvents(new BlockListener<BlockPlaceEvent>(), this);
    // getServer().getPluginManager().registerEvents(new BlockListener<BlockBreakEvent>(), this);
    getServer().getPluginManager().registerEvents(new BlockGravityListener(), this);
    getServer().getPluginManager().registerEvents(new BlockChangeListener(), this);
    // ProtocolManager manager = ProtocolLibrary.getProtocolManager();
    // manager.addPacketListener(new BlockChanger());
    getLogger().info("God turned on gravity!");
  }

  @Override
  public void onDisable() {
    getLogger().info("Gravity shutting down...");
  }
}
