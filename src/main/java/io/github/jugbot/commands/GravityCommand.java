package io.github.jugbot.commands;

import java.lang.ref.Reference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import io.github.jugbot.App;
import io.github.jugbot.BlockChangeEvent;
import io.github.jugbot.ChunkProcessor;

public class GravityCommand implements CommandExecutor {

  static class PlayerSender {
    static boolean onCommand(Player player, Command command, String label, List<String> args) {
      label = args.remove(0);
      switch (label) {
        case "update":
          {
            Block block = player.getLocation().getBlock();
            Bukkit.getPluginManager().callEvent(new BlockChangeEvent(block));
            return true;
          }
        case "reset":
          {
            Block block = player.getLocation().getBlock();
            ChunkProcessor.Instance().debugResetChunk(block.getChunk());
            return true;
          }
      }
      return false;
    }
  }

  static class ConsoleSender {
    static boolean onCommand(ConsoleCommandSender console, Command command, String label, List<String> args) {
      label = args.remove(0);
      switch (label) {
        case "update":
          {
            Location location = getLocation2DArgs(args);
            if (location == null) return false;
            Bukkit.getPluginManager().callEvent(new BlockChangeEvent(location.getBlock()));
            return true;
          }
        case "reset":
          {
            Location location = getLocation2DArgs(args);
            if (location == null) return false;
            ChunkProcessor.Instance().debugResetChunk(location.getChunk());
            return true;
          }
        case "load":
          {
            Location location = getLocation2DArgs(args);
            if (location == null) return false;
            location
                .getWorld()
                .addPluginChunkTicket(location.getBlockX() / 16, location.getBlockZ() / 16, App.Instance());
            return true;
          }
      }
      return false;
    }
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] _args) {
    List<String> args = new ArrayList<>(Arrays.asList(_args));
    if (sender instanceof Player) {
      return PlayerSender.onCommand((Player) sender, command, label, args);
    } else if (sender instanceof ConsoleCommandSender) {
      return ConsoleSender.onCommand((ConsoleCommandSender) sender, command, label, args);
    }
    return false;
  }

  private static Location getLocation2DArgs(List<String> args) {
    try {
      int i = 0;
      World world = Bukkit.getWorld(args.get(i++));
      double x = Double.valueOf(args.get(i++)).doubleValue();
      double y = 0.0;
      double z = Double.valueOf(args.get(i++)).doubleValue();
      return new Location(world, x, y, z);
    } catch (Exception e) {
      return null;
    }
  }
}