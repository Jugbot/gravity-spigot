package io.github.jugbot.graph;

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
  WEST_DEST(12);

  private int value;

  private ReservedID(int value) {
    this.value = value;
  }

  public int value() {
    return value;
  }
}
