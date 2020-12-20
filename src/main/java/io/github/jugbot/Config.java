package io.github.jugbot;

import org.bukkit.Material;
import org.bukkit.block.Block;

public class Config {
  protected static Config instance = null;

  public static Config Instance() {
    if (instance == null) instance = new Config();
    return instance;
  }

  protected Config() {
    //
  }

  public int getDestructionPerTick() {
    return 8;
  }

  public boolean isRootBlock(Block candidate) {
    return candidate.getType() == Material.BEDROCK;
  }
}
