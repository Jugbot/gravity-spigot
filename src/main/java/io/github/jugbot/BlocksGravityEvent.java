package io.github.jugbot;

import java.util.Collection;
import javax.annotation.Nonnull;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class BlocksGravityEvent extends Event {
  private static final HandlerList handlers = new HandlerList();
  private final Collection<Block> blocks;

  public BlocksGravityEvent(@Nonnull Collection<Block> blocks) {
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
  public final Collection<Block> getBlocks() {
    return blocks;
  }
}
