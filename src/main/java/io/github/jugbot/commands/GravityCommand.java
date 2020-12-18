package io.github.jugbot.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import io.github.jugbot.App;
import io.github.jugbot.BlockChangeEvent;
import io.github.jugbot.ChunkProcessor;
import io.github.jugbot.graph.Edge;
import io.github.jugbot.graph.ReservedID;
import io.github.jugbot.graph.SubGraph;
import io.github.jugbot.graph.Vertex;

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
        case "edges":
          {
            Block block =
                (args.size() > 0 && args.get(0).equals("here"))
                    ? player.getLocation().getBlock()
                    : player.getTargetBlockExact(16);
            SubGraph graph = ChunkProcessor.Instance().getChunkGraph(block.getChunk());
            Vertex inspected = new Vertex(block);
            player.sendMessage(inspected.toString());
            Edge[] outEdges = new Edge[8];
            Vertex[] neighbors =
                new Vertex[] {
                  new Vertex(block.getChunk(), ReservedID.SOURCE),
                  new Vertex(block.getChunk(), ReservedID.DEST),
                  new Vertex(block.getRelative(BlockFace.UP)),
                  new Vertex(block.getRelative(BlockFace.DOWN)),
                  new Vertex(block.getRelative(BlockFace.EAST)),
                  new Vertex(block.getRelative(BlockFace.WEST)),
                  new Vertex(block.getRelative(BlockFace.NORTH)),
                  new Vertex(block.getRelative(BlockFace.SOUTH))
                };

            int j = -1;
            if (graph.nodes().contains(neighbors[++j]))
              outEdges[j] = graph.edgeConnectingOrNull(neighbors[j], inspected);
            if (graph.nodes().contains(neighbors[++j]))
              outEdges[j] = graph.edgeConnectingOrNull(inspected, neighbors[j]);
            if (graph.nodes().contains(neighbors[++j]))
              outEdges[j] = graph.edgeConnectingOrNull(inspected, neighbors[j]);
            if (graph.nodes().contains(neighbors[++j]))
              outEdges[j] = graph.edgeConnectingOrNull(inspected, neighbors[j]);
            if (graph.nodes().contains(neighbors[++j]))
              outEdges[j] = graph.edgeConnectingOrNull(inspected, neighbors[j]);
            if (graph.nodes().contains(neighbors[++j]))
              outEdges[j] = graph.edgeConnectingOrNull(inspected, neighbors[j]);
            if (graph.nodes().contains(neighbors[++j]))
              outEdges[j] = graph.edgeConnectingOrNull(inspected, neighbors[j]);
            if (graph.nodes().contains(neighbors[++j]))
              outEdges[j] = graph.edgeConnectingOrNull(inspected, neighbors[j]);

            int i = -1;
            if (outEdges[++i] != null) player.sendMessage("MASS : " + outEdges[i].toString());
            if (outEdges[++i] != null) player.sendMessage("SINK : " + outEdges[i].toString());
            if (outEdges[++i] != null) player.sendMessage("UP   : " + outEdges[i].toString());
            if (outEdges[++i] != null) player.sendMessage("DOWN : " + outEdges[i].toString());
            if (outEdges[++i] != null) player.sendMessage("EAST : " + outEdges[i].toString());
            if (outEdges[++i] != null) player.sendMessage("WEST : " + outEdges[i].toString());
            if (outEdges[++i] != null) player.sendMessage("NORTH: " + outEdges[i].toString());
            if (outEdges[++i] != null) player.sendMessage("SOUTH: " + outEdges[i].toString());
            return true;
            // List<Edge> edges = iChunk.debugGetEdgesAt(block);
            // for (IntegrityData type : IntegrityData.values()) {
            //   Edge edge = edges.get(type.ordinal());
            //   if (edge == null) player.sendMessage(type.name() + ": null");
            //   else player.sendMessage(type.name() + ": " + edge.cap + "c " + edge.f + "f");
            // }
            // for (int i = IntegrityData.values().length; i < edges.size(); i++) {
            //   Edge edge = edges.get(i);
            //   player.sendMessage(edge.t + ": " + edge.cap + "c " + edge.f + "f");
            // }
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
