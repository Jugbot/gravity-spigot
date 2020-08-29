package io.github.jugbot;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

public class BlockData implements ConfigurationSerializable {
  Map<Material, EnumMap<IntegrityData, Integer>> blocks;
  EnumMap<IntegrityData, Integer> defaultBlock;
  EnumMap<IntegrityData, Integer> emptyBlock;

  BlockData() {
    blocks = new HashMap<Material, EnumMap<IntegrityData, Integer>>();
  }

  BlockData(Map<String, Object> serialized) {
    App.Instance().getLogger().info("Initializing blockmap...");
    blocks = new HashMap<Material, EnumMap<IntegrityData, Integer>>();
    for (Map.Entry<String, Object> kv : serialized.entrySet()) {
      Material material = Material.matchMaterial(kv.getKey());
      App.Instance().getLogger().fine(material.toString());
      Map<String, Object> weights;
      if (kv.getValue() instanceof MemorySection) {
        weights = ((MemorySection) kv.getValue()).getValues(false);
      } else {
        weights = (Map<String, Object>) kv.getValue();
      }

      EnumMap<IntegrityData, Integer> data = new EnumMap(IntegrityData.class);
      data.put(IntegrityData.MASS, (int) weights.get("mass"));
      data.put(IntegrityData.UP, (int) weights.get("u"));
      data.put(IntegrityData.DOWN, (int) weights.get("d"));
      data.put(IntegrityData.NORTH, (int) weights.get("n"));
      data.put(IntegrityData.EAST, (int) weights.get("e"));
      data.put(IntegrityData.SOUTH, (int) weights.get("s"));
      data.put(IntegrityData.WEST, (int) weights.get("w"));

      blocks.put(material, data);
    }
  }

  @Override
  public Map<String, Object> serialize() {
    Map<String, Object> map = new HashMap<>();
    for (Map.Entry<Material, EnumMap<IntegrityData, Integer>> kv : blocks.entrySet()) {
      Map<String, Integer> blockData = new HashMap<>();
      blockData.put("mass", kv.getValue().get(IntegrityData.MASS));
      blockData.put("u", kv.getValue().get(IntegrityData.UP));
      blockData.put("d", kv.getValue().get(IntegrityData.DOWN));
      blockData.put("n", kv.getValue().get(IntegrityData.NORTH));
      blockData.put("e", kv.getValue().get(IntegrityData.EAST));
      blockData.put("s", kv.getValue().get(IntegrityData.SOUTH));
      blockData.put("w", kv.getValue().get(IntegrityData.WEST));
      map.put(kv.getKey().name(), blockData);
    }
    return map;
  }

  public EnumMap<IntegrityData, Integer> getData(Material material) {
    return blocks.get(material);
  }

  public EnumMap<IntegrityData, Integer> getEmpty() {
    if (emptyBlock == null) {
      emptyBlock = new EnumMap(IntegrityData.class);
      emptyBlock.put(IntegrityData.MASS, 0);
      emptyBlock.put(IntegrityData.UP, 0);
      emptyBlock.put(IntegrityData.DOWN, 0);
      emptyBlock.put(IntegrityData.NORTH, 0);
      emptyBlock.put(IntegrityData.EAST, 0);
      emptyBlock.put(IntegrityData.SOUTH, 0);
      emptyBlock.put(IntegrityData.WEST, 0);
    }
    return emptyBlock;
  }

  public EnumMap<IntegrityData, Integer> getDefault() {
    if (defaultBlock == null) {
      defaultBlock = new EnumMap(IntegrityData.class);
      defaultBlock.put(IntegrityData.MASS, 1);
      defaultBlock.put(IntegrityData.UP, Integer.MAX_VALUE);
      defaultBlock.put(IntegrityData.DOWN, Integer.MAX_VALUE);
      defaultBlock.put(IntegrityData.NORTH, Integer.MAX_VALUE);
      defaultBlock.put(IntegrityData.EAST, Integer.MAX_VALUE);
      defaultBlock.put(IntegrityData.SOUTH, Integer.MAX_VALUE);
      defaultBlock.put(IntegrityData.WEST, Integer.MAX_VALUE);
    }
    return defaultBlock;
  }
}
