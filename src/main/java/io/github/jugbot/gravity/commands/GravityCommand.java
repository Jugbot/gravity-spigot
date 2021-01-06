package io.github.jugbot.gravity.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

public class GravityCommand implements CommandExecutor {

  static class PlayerSender {
    static boolean onCommand(Player player, Command command, String label, List<String> args) {
      label = args.remove(0);
      switch (label) {
      }
      return false;
    }
  }

  static class ConsoleSender {
    static boolean onCommand(ConsoleCommandSender console, Command command, String label, List<String> args) {
      label = args.remove(0);
      switch (label) {
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
