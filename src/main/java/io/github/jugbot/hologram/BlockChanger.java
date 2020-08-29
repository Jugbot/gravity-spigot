package io.github.jugbot.hologram;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;

import io.github.jugbot.App;

public class BlockChanger extends PacketAdapter {
  // https://github.com/Huskehhh/FakeBlock/blob/b52d72e926d3924c11799990f68d1bb44b40a3fc/latest/src/main/java/pro/husk/fakeblock/objects/MaterialWall.java
  // PacketContainer fakeChunk = new PacketContainer(PacketType.Play.Server.MULTI_BLOCK_CHANGE);

  public BlockChanger() {
    super(App.Instance(), ListenerPriority.LOW, PacketType.Play.Server.MULTI_BLOCK_CHANGE);
  }

  @Override
  public void onPacketSending(PacketEvent event) {
    App.Instance().getLogger().fine("Block Place Event");
    // event.get
    // Bukkit.getPluginManager().callEvent(new BlockChangeEvent(event.getBlock()));
  }
}
