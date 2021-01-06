package io.github.jugbot.gravity.util;

import java.lang.reflect.Field;

import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_16_R2.CraftWorld;

import net.minecraft.server.v1_16_R2.Block;
import net.minecraft.server.v1_16_R2.BlockPosition;
import net.minecraft.server.v1_16_R2.MinecraftKey;
import net.minecraft.server.v1_16_R2.SoundEffect;
import net.minecraft.server.v1_16_R2.SoundEffectType;
import net.minecraft.server.v1_16_R2.WorldServer;

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
