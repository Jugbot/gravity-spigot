package io.github.jugbot;

import org.bukkit.plugin.java.JavaPlugin;

/**
 * Hello world!
 *
 */
public class App extends JavaPlugin
{
    static App instance;

    @Override
    public void onEnable() {
        instance = this;
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
