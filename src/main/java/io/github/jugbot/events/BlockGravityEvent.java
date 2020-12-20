package io.github.jugbot.events;

import javax.annotation.Nonnull;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class BlockGravityEvent extends Event {
  private static final HandlerList handlers = new HandlerList();
  private final Block block;

  public BlockGravityEvent(@Nonnull Block block) {
    super();
    this.block = block;
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
  public final Block getBlocks() {
    return block;
  }
}
