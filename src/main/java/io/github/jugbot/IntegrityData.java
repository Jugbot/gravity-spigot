package io.github.jugbot;

public enum IntegrityData {
  // Do not change order
  MASS,
  UP,
  DOWN,
  NORTH,
  EAST,
  SOUTH,
  WEST;
  // Cardinal opposite
  public IntegrityData opposite() {
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
}
