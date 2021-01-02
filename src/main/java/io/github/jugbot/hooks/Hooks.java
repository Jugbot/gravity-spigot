package io.github.jugbot.hooks;

import java.util.ArrayList;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;

public class Hooks {
  private static Hooks instance = null;
  private ArrayList<ProtectionPluginHook> protectionHooks = new ArrayList<>();

  public static Hooks Instance() {
    if (instance == null) {
      instance = new Hooks();
    }
    return instance;
  }

  private Hooks() {
    if (Bukkit.getServer().getPluginManager().getPlugin("WorldGuard") != null) {
      protectionHooks.add(new WorldGuardHook());
    }
  }

  public boolean canDestroyBlock(Block paramBlock) {
    for (ProtectionPluginHook protectionPluginHook : this.protectionHooks) {
      if (!protectionPluginHook.canDestroy(paramBlock)) return false;
    }
    return true;
  }
}
