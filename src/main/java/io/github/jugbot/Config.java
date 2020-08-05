package io.github.jugbot;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.CharBuffer;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import com.google.common.base.Charsets;

public class Config {
  private static Config instance = null;
  private File blockDataConfigFile;
  private BlockData blockData;

  public static Config Instance() {
    if (instance == null)
      instance = new Config();
    return instance;
  }

  private Config() {
    blockDataConfigFile = new File(App.instance.getDataFolder(), "blockdata.yml"); 
    if (!blockDataConfigFile.exists()) {
      System.out.println("blockdata.yml doesn't exist, creating it...");
      blockDataConfigFile.getParentFile().mkdirs();
      App.instance.saveResource("blockdata.yml", false);
    }
    FileConfiguration blockDataConfig;
    try {
      blockDataConfig = YamlConfiguration.loadConfiguration(blockDataConfigFile);
      blockData = new BlockData(blockDataConfig.getConfigurationSection("root.blocks").getValues(false));
    } catch (IllegalArgumentException e) {
      e.printStackTrace();
    }
    if (blockData == null) {
      System.out.println("No block data found in blockdata.yml!");
      blockData = new BlockData();
    };
  }

  public BlockData getBlockData() {
    return blockData;
  }
}