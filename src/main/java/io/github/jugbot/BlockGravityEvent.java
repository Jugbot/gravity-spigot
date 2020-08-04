package io.github.jugbot;

import javax.annotation.Nonnull;

import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockEvent;

public class BlockGravityEvent extends Event {
  private static final HandlerList handlers = new HandlerList();
  private final Block[] blocks;

  public BlockGravityEvent(@Nonnull Block[] blocks) {
    super();
    this.blocks = blocks;
  }

  @Override
  @Nonnull
  public HandlerList getHandlers() {
      return handlers;
  }

  @Nonnull
  public static HandlerList getHandlerList() {
      return handlers;
  }
  
  @Nonnull
  public final Block[] getBlocks() {
      return blocks;
  }
}