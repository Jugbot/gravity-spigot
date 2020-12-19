package io.github.jugbot;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;

import com.google.common.base.Charsets;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class Config {
  protected static Config instance = null;
  protected IntegrityData blockData = new IntegrityData();;
  private int maxChunkDistance = 5;

  public static Config Instance() {
    if (instance == null) instance = new Config();
    return instance;
  }

  protected Config() {
    loadBlockData();
  }

  protected void loadBlockData() {
    // YML data
    File blockDataConfigFile = new File(App.Instance().getDataFolder(), "blockdata.yml");
    if (!blockDataConfigFile.exists()) {
      App.Instance().getLogger().info("blockdata.yml doesn't exist, creating it...");
      blockDataConfigFile.getParentFile().mkdirs();
      App.Instance().saveResource("blockdata.yml", false);
    }
    loadBlockDataYAML(blockDataConfigFile);
    // CSV data
    blockDataConfigFile = new File(App.Instance().getDataFolder(), "blockdata.csv");
    if (!blockDataConfigFile.exists()) {
      App.Instance().getLogger().info("blockdata.csv doesn't exist, creating it...");
      blockDataConfigFile.getParentFile().mkdirs();
      App.Instance().saveResource("blockdata.csv", false);
    }
    loadBlockDataCSV(blockDataConfigFile);

    if (blockData.blocks.size() == 0) {
      App.Instance().getLogger().info("No block data found in blockdata config!");
      blockData = new IntegrityData();
    }
  }

  private void loadBlockDataYAML(File blockDataConfigFile) {
    FileConfiguration blockDataConfig;
    try {
      blockDataConfig = YamlConfiguration.loadConfiguration(blockDataConfigFile);
      blockData = new IntegrityData(blockDataConfig.getConfigurationSection("root.blocks").getValues(false));
    } catch (IllegalArgumentException e) {
      e.printStackTrace();
    }
  }

  private void loadBlockDataCSV(File blockDataConfigFile) {
    ArrayList<Material> conflicting = new ArrayList<>();
    try (CSVParser parser = CSVParser.parse(blockDataConfigFile, Charsets.UTF_8, CSVFormat.DEFAULT)) {
      for (CSVRecord record : parser.getRecords()) {
        App.Instance().getLogger().fine(record.toString());
        Material material = Material.getMaterial(record.get(0));
        if (material == null) {
          if (record.getRecordNumber() != 1) {
            App.Instance()
                .getLogger()
                .info(
                    "[blockdata.csv:("
                        + record.getRecordNumber()
                        + ")] Material \""
                        + record.get(0)
                        + "\" is not a recognizable material name, skipping.");
          }
          continue;
        }
        EnumMap<Integrity, Float> data;
        try {
          data = new EnumMap<>(Integrity.class);
          data.put(Integrity.MASS, Float.parseFloat(record.get(1)));
          data.put(Integrity.UP, Float.parseFloat(record.get(2)));
          data.put(Integrity.DOWN, Float.parseFloat(record.get(3)));
          data.put(Integrity.NORTH, Float.parseFloat(record.get(4)));
          data.put(Integrity.EAST, Float.parseFloat(record.get(5)));
          data.put(Integrity.SOUTH, Float.parseFloat(record.get(6)));
          data.put(Integrity.WEST, Float.parseFloat(record.get(7)));
        } catch (NumberFormatException e) {
          App.Instance()
              .getLogger()
              .info(
                  "[blockdata.csv:("
                      + record.getRecordNumber()
                      + ")] Material \""
                      + record.get(0)
                      + "\" must have data that is a positive int, skipping.");
          continue;
        }
        if (blockData.blocks.containsKey(material)) {
          conflicting.add(material);
        }
        blockData.blocks.put(material, data);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    if (!conflicting.isEmpty()) {
      App.Instance().getLogger().warning("Blockdata yml and csv have conflicting integrity data:");
      App.Instance()
          .getLogger()
          .warning(conflicting.stream().map(mat -> mat.name()).reduce((a, b) -> a + ", " + b).get());
    }
  }

  public EnumMap<Integrity, Float> getStructuralData(Material material) {
    if (!isStructural(material)) return blockData.getEmpty();
    EnumMap<Integrity, Float> data = blockData.getData(material);
    if (data == null) return blockData.getDefault();
    return data;
  }

  public int getMaxChunkDistance() {
    return maxChunkDistance;
  }

  public boolean isStructural(Material material) {
    return material.isSolid();
  }

  public int getMaximumUpdates() {
    return 8;
  }
}
