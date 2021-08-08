package io.github.jugbot.gravity.hooks;

import org.bukkit.block.Block;

public interface ProtectionPluginHook {

  public boolean canDestroy(Block block);
}
