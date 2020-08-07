package io.github.jugbot;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;



public class BlockData implements ConfigurationSerializable {
  Map<Material, int[]> blocks;

  BlockData() {
    blocks = new HashMap<Material, int[]>();
  }

  BlockData(Map<String, Object> serialized) {
    System.out.println("Initializing blockmap...");
    blocks = new HashMap<Material, int[]>();
    for (Map.Entry<String, Object> kv : serialized.entrySet()) {
      Material material = Material.matchMaterial(kv.getKey());
      System.out.println(material);
      Map<String, Object> weights;
      if (kv.getValue() instanceof MemorySection) {
        weights = ((MemorySection) kv.getValue()).getValues(false);
      } else {
        weights = (Map<String, Object>) kv.getValue();
      }
      
      blocks.put(material, new int[] {
          (int) weights.get("mass"),
        (int) weights.get("u"),
        (int) weights.get("d"),
        (int) weights.get("n"),
        (int) weights.get("e"),
        (int) weights.get("s"),
        (int) weights.get("w")
      });
    }
  }

  @Override
  public Map<String, Object> serialize() {
    Map<String, Object> map = new HashMap<>();
    for (Map.Entry<Material, int[]> kv : blocks.entrySet()) {
      Map<String, Integer> blockData = new HashMap<>();
      blockData.put("mass", kv.getValue()[0]);
      blockData.put("u", kv.getValue()[1]);
      blockData.put("d", kv.getValue()[2]);
      blockData.put("n", kv.getValue()[3]);
      blockData.put("e", kv.getValue()[4]);
      blockData.put("s", kv.getValue()[5]);
      blockData.put("w", kv.getValue()[6]);
      map.put(kv.getKey().name(), blockData);
    }
    return map;
  }

  public int[] getData(Material material) {
    return blocks.get(material);
  }
  
}