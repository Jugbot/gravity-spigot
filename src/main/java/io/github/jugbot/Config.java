package io.github.jugbot;

import java.io.File;
import java.io.IOException;
import java.util.EnumMap;

import com.google.common.base.Charsets;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class Config {
  private static Config instance = null;
  private IntegrityData blockData;

  public static Config Instance() {
    if (instance == null) instance = new Config();
    return instance;
  }

  private Config() {
    loadBlockData();
  }

  private void loadBlockData() {
    File blockDataConfigFile;
    if ((blockDataConfigFile = new File(App.Instance().getDataFolder(), "blockdata.csv")).exists()) {
      loadBlockDataCSV(blockDataConfigFile);
    } else if ((blockDataConfigFile = new File(App.Instance().getDataFolder(), "blockdata.yml")).exists()) {
      loadBlockDataYAML(blockDataConfigFile);
    } else {
      App.Instance().getLogger().info("blockdata.yml doesn't exist, creating it...");
      blockDataConfigFile.getParentFile().mkdirs();
      App.Instance().saveResource("blockdata.yml", false);
      loadBlockDataYAML(blockDataConfigFile);
    }
    if (blockData == null || blockData.blocks.size() == 0) {
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
    blockData = new IntegrityData();
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
        blockData.blocks.put(material, data);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static EnumMap<Integrity, Float> getStructuralData(Material material) {
    if (!isStructural(material)) return Instance().blockData.getEmpty();
    EnumMap<Integrity, Float> data = Instance().blockData.getData(material);
    if (data == null) return Instance().blockData.getDefault();
    return data;
  }

  public static boolean isStructural(Material material) {
    return material.isSolid();
  }
}
