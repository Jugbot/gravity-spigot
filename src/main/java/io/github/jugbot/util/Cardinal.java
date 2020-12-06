package io.github.jugbot.util;

public enum Cardinal {
  NORTH(0, -1),
  EAST(1, 0),
  SOUTH(0, 1),
  WEST(-1, 0);

  Cardinal(int x, int z) {}

  public Cardinal opposite() {
    switch (this) {
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

  public static Cardinal from(int x, int z) {
    if (x == 0 && z == -1) return NORTH;
    if (x == 1 && z == 0) return EAST;
    if (x == 0 && z == 1) return SOUTH;
    if (x == -1 && z == 0) return WEST;
    return null;
  }
}
