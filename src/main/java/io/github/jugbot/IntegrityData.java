package io.github.jugbot;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

public class IntegrityData implements ConfigurationSerializable {
  Map<Material, EnumMap<Integrity, Float>> blocks;
  EnumMap<Integrity, Float> defaultBlock;
  EnumMap<Integrity, Float> emptyBlock;

  IntegrityData() {
    blocks = new HashMap<Material, EnumMap<Integrity, Float>>();
  }

  IntegrityData(Map<String, Object> serialized) {
    App.Instance().getLogger().info("Initializing blockmap...");
    blocks = new HashMap<Material, EnumMap<Integrity, Float>>();
    for (Map.Entry<String, Object> kv : serialized.entrySet()) {
      Material material = Material.matchMaterial(kv.getKey());
      App.Instance().getLogger().fine(material.toString());
      Map<String, Object> weights;
      if (kv.getValue() instanceof MemorySection) {
        weights = ((MemorySection) kv.getValue()).getValues(false);
      } else {
        weights = (Map<String, Object>) kv.getValue();
      }

      EnumMap<Integrity, Float> data = new EnumMap(Integrity.class);
      data.put(Integrity.MASS, ((Integer) weights.get("mass")).floatValue());
      data.put(Integrity.UP, ((Integer) weights.get("u")).floatValue());
      data.put(Integrity.DOWN, ((Integer) weights.get("d")).floatValue());
      data.put(Integrity.NORTH, ((Integer) weights.get("n")).floatValue());
      data.put(Integrity.EAST, ((Integer) weights.get("e")).floatValue());
      data.put(Integrity.SOUTH, ((Integer) weights.get("s")).floatValue());
      data.put(Integrity.WEST, ((Integer) weights.get("w")).floatValue());

      blocks.put(material, data);
    }
  }

  @Override
  public Map<String, Object> serialize() {
    Map<String, Object> map = new HashMap<>();
    for (Map.Entry<Material, EnumMap<Integrity, Float>> kv : blocks.entrySet()) {
      Map<String, Float> blockData = new HashMap<>();
      blockData.put("mass", kv.getValue().get(Integrity.MASS));
      blockData.put("u", kv.getValue().get(Integrity.UP));
      blockData.put("d", kv.getValue().get(Integrity.DOWN));
      blockData.put("n", kv.getValue().get(Integrity.NORTH));
      blockData.put("e", kv.getValue().get(Integrity.EAST));
      blockData.put("s", kv.getValue().get(Integrity.SOUTH));
      blockData.put("w", kv.getValue().get(Integrity.WEST));
      map.put(kv.getKey().name(), blockData);
    }
    return map;
  }

  public EnumMap<Integrity, Float> getData(Material material) {
    return blocks.get(material);
  }

  public EnumMap<Integrity, Float> getEmpty() {
    if (emptyBlock == null) {
      emptyBlock = new EnumMap<>(Integrity.class);
      emptyBlock.put(Integrity.MASS, 0f);
      emptyBlock.put(Integrity.UP, 0f);
      emptyBlock.put(Integrity.DOWN, 0f);
      emptyBlock.put(Integrity.NORTH, 0f);
      emptyBlock.put(Integrity.EAST, 0f);
      emptyBlock.put(Integrity.SOUTH, 0f);
      emptyBlock.put(Integrity.WEST, 0f);
    }
    return emptyBlock;
  }

  public EnumMap<Integrity, Float> getDefault() {
    if (defaultBlock == null) {
      defaultBlock = new EnumMap<>(Integrity.class);
      defaultBlock.put(Integrity.MASS, 1f);
      defaultBlock.put(Integrity.UP, Float.POSITIVE_INFINITY);
      defaultBlock.put(Integrity.DOWN, Float.POSITIVE_INFINITY);
      defaultBlock.put(Integrity.NORTH, Float.POSITIVE_INFINITY);
      defaultBlock.put(Integrity.EAST, Float.POSITIVE_INFINITY);
      defaultBlock.put(Integrity.SOUTH, Float.POSITIVE_INFINITY);
      defaultBlock.put(Integrity.WEST, Float.POSITIVE_INFINITY);
    }
    return defaultBlock;
  }
}
