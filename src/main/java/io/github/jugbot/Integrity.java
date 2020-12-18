package io.github.jugbot;

import io.github.jugbot.util.Cardinal;

public enum Integrity {
  // Do not change order
  MASS,
  UP,
  DOWN,
  NORTH,
  EAST,
  SOUTH,
  WEST;
  // Cardinal opposite
  public Integrity opposite() {
    switch (this) {
      case UP:
        return DOWN;
      case DOWN:
        return UP;
      case NORTH:
        return SOUTH;
      case SOUTH:
        return NORTH;
      case EAST:
        return WEST;
      case WEST:
        return EAST;
      default:
        return null;
    }
  }

  public static Integrity from(Cardinal cardinal) {
    switch (cardinal) {
      case NORTH:
        return NORTH;
      case SOUTH:
        return SOUTH;
      case EAST:
        return EAST;
      case WEST:
        return WEST;
      default:
        return null;
    }
  }
}
