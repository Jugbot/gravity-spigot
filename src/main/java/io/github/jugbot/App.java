package io.github.jugbot;

import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Hello world!
 *
 */
public class App extends JavaPlugin
{
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
        getServer().getPluginManager().registerEvents(new BlockEventListeners(), this);
        getServer().getPluginManager().registerEvents(new BlockGravityListener(), this);
        getServer().getPluginManager().registerEvents(new ChunkPreparer(), this);
        getLogger().info("God turned on gravity!");
    }
    
    @Override
    public void onDisable() {
        getLogger().info("Gravity shutting down...");
    }
}