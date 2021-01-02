package io.github.jugbot.hooks;

import org.bukkit.block.Block;

public interface ProtectionPluginHook {

  public boolean canDestroy(Block block);
}
