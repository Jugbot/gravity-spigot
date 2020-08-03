package io.github.jugbot.util;

import java.lang.reflect.Field;

import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_15_R1.CraftWorld;

import net.minecraft.server.v1_15_R1.Block;
import net.minecraft.server.v1_15_R1.BlockPosition;
import net.minecraft.server.v1_15_R1.MinecraftKey;
import net.minecraft.server.v1_15_R1.SoundEffect;
import net.minecraft.server.v1_15_R1.SoundEffectType;
import net.minecraft.server.v1_15_R1.WorldServer;

public class SoundUtil {
  public Sound getSound(org.bukkit.block.Block block) {
    try {
      WorldServer nmsWorld = ((CraftWorld) block.getWorld()).getHandle();
      Block nmsBlock = nmsWorld.getType(new BlockPosition(block.getX(), block.getY(), block.getZ())).getBlock();
      SoundEffectType soundEffectType = nmsBlock.getStepSound(nmsBlock.getBlockData());

      Field breakSound = SoundEffectType.class.getDeclaredField("y");
      breakSound.setAccessible(true);
      SoundEffect nmsSound = (SoundEffect) breakSound.get(soundEffectType);

      Field keyField = SoundEffect.class.getDeclaredField("a");
      keyField.setAccessible(true);
      MinecraftKey nmsString = (MinecraftKey) keyField.get(nmsSound);

      return Sound.valueOf(nmsString.getKey().replace(".", "_").toUpperCase());
    } catch (IllegalAccessException | NoSuchFieldException ex) {
        ex.printStackTrace();
    }
    return null;
  }
}