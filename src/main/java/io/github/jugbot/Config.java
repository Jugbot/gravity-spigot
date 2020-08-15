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
  private BlockData blockData;

  public static Config Instance() {
    if (instance == null) instance = new Config();
    return instance;
  }

  private Config() {
    File blockDataConfigFile;
    if ((blockDataConfigFile = new File(App.Instance().getDataFolder(), "blockdata.csv")).exists()) {
      loadBlockDataCSV(blockDataConfigFile);
    } else if ((blockDataConfigFile = new File(App.Instance().getDataFolder(), "blockdata.yml")).exists()) {
      loadBlockDataYAML(blockDataConfigFile);
    } else {
      System.out.println("blockdata.yml doesn't exist, creating it...");
      blockDataConfigFile.getParentFile().mkdirs();
      App.Instance().saveResource("blockdata.yml", false);
      loadBlockDataYAML(blockDataConfigFile);
    }
    if (blockData == null || blockData.blocks.size() == 0) {
      App.Instance().getLogger().info("No block data found in blockdata config!");
      blockData = new BlockData();
    }
  }

  public BlockData getBlockData() {
    return blockData;
  }

  private void loadBlockDataYAML(File blockDataConfigFile) {
    FileConfiguration blockDataConfig;
    try {
      blockDataConfig = YamlConfiguration.loadConfiguration(blockDataConfigFile);
      blockData = new BlockData(blockDataConfig.getConfigurationSection("root.blocks").getValues(false));
    } catch (IllegalArgumentException e) {
      e.printStackTrace();
    }
  }

  private void loadBlockDataCSV(File blockDataConfigFile) {
    blockData = new BlockData();
    try (CSVParser parser = CSVParser.parse(blockDataConfigFile, Charsets.UTF_8, CSVFormat.DEFAULT)) {
      for (CSVRecord record : parser.getRecords()) {
        System.out.println(record.toString());
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
        EnumMap<IntegrityData, Integer> data;
        try {
          data = new EnumMap(IntegrityData.class);
          data.put(IntegrityData.MASS, Integer.parseUnsignedInt(record.get(1)));
          data.put(IntegrityData.UP, Integer.parseUnsignedInt(record.get(2)));
          data.put(IntegrityData.DOWN, Integer.parseUnsignedInt(record.get(3)));
          data.put(IntegrityData.NORTH, Integer.parseUnsignedInt(record.get(4)));
          data.put(IntegrityData.EAST, Integer.parseUnsignedInt(record.get(5)));
          data.put(IntegrityData.SOUTH, Integer.parseUnsignedInt(record.get(6)));
          data.put(IntegrityData.WEST, Integer.parseUnsignedInt(record.get(7)));
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
}
