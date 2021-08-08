package io.github.jugbot.gravity.hooks;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import io.github.jugbot.gravity.App;
import org.bukkit.block.Block;

public class WorldGuardHook implements ProtectionPluginHook {
  public static final StateFlag GRAVITY_FLAG = new StateFlag("gravity-plugin", true);

  WorldGuardHook() {
    FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
    try {
      // register our flag with the registry
      registry.register(GRAVITY_FLAG);
    } catch (FlagConflictException e) {
      // some other plugin registered a flag by the same name already.
      // you may want to re-register with a different name, but this
      // could cause issues with saved flags in region files. it's better
      // to print a message to let the server admin know of the conflict
      App.Instance().getLogger().warning("Conflicting worldguard flag: gravity-plugin");
    }
  }

  public boolean canDestroy(Block paramBlock) {
    WorldGuard worldGuardPlugin = WorldGuard.getInstance();
    // return worldGuardPlugin.canBuild(null, paramBlock);
    RegionContainer container = worldGuardPlugin.getPlatform().getRegionContainer();
    RegionQuery regions = container.createQuery();
    // Check to make sure that "regions" is not null
    ApplicableRegionSet set = regions.getApplicableRegions(BukkitAdapter.adapt(paramBlock.getLocation()));
    if (!set.testState(null, GRAVITY_FLAG)) {
      return false;
    }
    return true;
  }
}
