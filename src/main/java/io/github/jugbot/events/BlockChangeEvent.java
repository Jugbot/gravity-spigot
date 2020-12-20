package io.github.jugbot.events;

import javax.annotation.Nonnull;

import org.bukkit.block.Block;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockEvent;

public class BlockChangeEvent extends BlockEvent {
  private static final HandlerList handlers = new HandlerList();

  public BlockChangeEvent(@Nonnull Block block) {
    super(block);
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
}
