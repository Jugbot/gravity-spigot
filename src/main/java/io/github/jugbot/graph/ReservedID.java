package io.github.jugbot.graph;

import io.github.jugbot.util.Cardinal;

public enum ReservedID {
  TEMP_SOURCE(1),
  TEMP_DEST(2),
  SOURCE(3),
  DEST(4),
  NORTH_SOURCE(5),
  EAST_SOURCE(6),
  SOUTH_SOURCE(7),
  WEST_SOURCE(8),
  NORTH_DEST(9),
  EAST_DEST(10),
  SOUTH_DEST(11),
  WEST_DEST(12),
  SUPER_SOURCE(13),
  SUPER_DEST(14);

  private int value;

  private ReservedID(int value) {
    this.value = value;
  }

  public int value() {
    return value;
  }

  public static ReservedID sourceFrom(Cardinal cardinal) {
    switch (cardinal) {
      case NORTH:
        return NORTH_SOURCE;
      case SOUTH:
        return SOUTH_SOURCE;
      case EAST:
        return EAST_SOURCE;
      case WEST:
        return WEST_SOURCE;
      default:
        return null;
    }
  }

  public static ReservedID destFrom(Cardinal cardinal) {
    switch (cardinal) {
      case NORTH:
        return NORTH_DEST;
      case SOUTH:
        return SOUTH_DEST;
      case EAST:
        return EAST_DEST;
      case WEST:
        return WEST_DEST;
      default:
        return null;
    }
  }
}
