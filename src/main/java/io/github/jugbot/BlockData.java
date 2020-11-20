package io.github.jugbot;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

public class BlockData implements ConfigurationSerializable {
  Map<Material, EnumMap<IntegrityData, Float>> blocks;
  EnumMap<IntegrityData, Float> defaultBlock;
  EnumMap<IntegrityData, Float> emptyBlock;

  BlockData() {
    blocks = new HashMap<Material, EnumMap<IntegrityData, Float>>();
  }

  BlockData(Map<String, Object> serialized) {
    App.Instance().getLogger().info("Initializing blockmap...");
    blocks = new HashMap<Material, EnumMap<IntegrityData, Float>>();
    for (Map.Entry<String, Object> kv : serialized.entrySet()) {
      Material material = Material.matchMaterial(kv.getKey());
      App.Instance().getLogger().fine(material.toString());
      Map<String, Object> weights;
      if (kv.getValue() instanceof MemorySection) {
        weights = ((MemorySection) kv.getValue()).getValues(false);
      } else {
        weights = (Map<String, Object>) kv.getValue();
      }

      EnumMap<IntegrityData, Float> data = new EnumMap(IntegrityData.class);
      data.put(IntegrityData.MASS, (float) weights.get("mass"));
      data.put(IntegrityData.UP, (float) weights.get("u"));
      data.put(IntegrityData.DOWN, (float) weights.get("d"));
      data.put(IntegrityData.NORTH, (float) weights.get("n"));
      data.put(IntegrityData.EAST, (float) weights.get("e"));
      data.put(IntegrityData.SOUTH, (float) weights.get("s"));
      data.put(IntegrityData.WEST, (float) weights.get("w"));

      blocks.put(material, data);
    }
  }

  @Override
  public Map<String, Object> serialize() {
    Map<String, Object> map = new HashMap<>();
    for (Map.Entry<Material, EnumMap<IntegrityData, Float>> kv : blocks.entrySet()) {
      Map<String, Float> blockData = new HashMap<>();
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

  public EnumMap<IntegrityData, Float> getData(Material material) {
    return blocks.get(material);
  }

  public EnumMap<IntegrityData, Float> getEmpty() {
    if (emptyBlock == null) {
      emptyBlock = new EnumMap(IntegrityData.class);
      emptyBlock.put(IntegrityData.MASS, 0f);
      emptyBlock.put(IntegrityData.UP, 0f);
      emptyBlock.put(IntegrityData.DOWN, 0f);
      emptyBlock.put(IntegrityData.NORTH, 0f);
      emptyBlock.put(IntegrityData.EAST, 0f);
      emptyBlock.put(IntegrityData.SOUTH, 0f);
      emptyBlock.put(IntegrityData.WEST, 0f);
    }
    return emptyBlock;
  }

  public EnumMap<IntegrityData, Float> getDefault() {
    if (defaultBlock == null) {
      defaultBlock = new EnumMap(IntegrityData.class);
      defaultBlock.put(IntegrityData.MASS, 1f);
      defaultBlock.put(IntegrityData.UP, Float.POSITIVE_INFINITY);
      defaultBlock.put(IntegrityData.DOWN, Float.POSITIVE_INFINITY);
      defaultBlock.put(IntegrityData.NORTH, Float.POSITIVE_INFINITY);
      defaultBlock.put(IntegrityData.EAST, Float.POSITIVE_INFINITY);
      defaultBlock.put(IntegrityData.SOUTH, Float.POSITIVE_INFINITY);
      defaultBlock.put(IntegrityData.WEST, Float.POSITIVE_INFINITY);
    }
    return defaultBlock;
  }
}
