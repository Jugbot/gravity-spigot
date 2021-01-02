package io.github.jugbot;

import io.github.jugbot.hooks.Hooks;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.bukkit.Material;
import org.bukkit.block.Block;

public class Config {
  protected static Config instance = null;

  static class Data {
    static Set<Material> rootBlocks = EnumSet.noneOf(Material.class);
    static int destructionPerTick;
  }

  public static Config Instance() {
    if (instance == null) instance = new Config();
    return instance;
  }

  protected Config() {
    App.Instance().getConfig().addDefault("blocks_destroyed_per_tick", 8);
    App.Instance().getConfig().addDefault("root_blocks", new String[] {Material.BEDROCK.name()});
    App.Instance().getConfig().options().copyDefaults(true);
    App.Instance().saveConfig();
    List<String> materialNames = App.Instance().getConfig().getStringList("root_blocks");
    Data.rootBlocks = materialNames.stream().map(Material::getMaterial).collect(Collectors.toSet());
    Data.destructionPerTick = App.Instance().getConfig().getInt("blocks_destroyed_per_tick");
  }

  public int getDestructionPerTick() {
    return Config.Data.destructionPerTick;
  }

  public boolean isRootBlock(Block candidate) {
    return Config.Data.rootBlocks.contains(candidate.getType());
  }

  public boolean blockCanFall(Block candidate) {
    return Hooks.Instance().canDestroyBlock(candidate);
  }
}
