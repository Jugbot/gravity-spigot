package io.github.jugbot.gravity.util;

import java.util.Objects;

public class IntegerXYZ {
  public int x, y, z;

  public IntegerXYZ(int x, int y, int z) {
    this.x = x;
    this.y = y;
    this.z = z;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof IntegerXYZ)) return false;
    IntegerXYZ other = (IntegerXYZ) obj;
    return this.x == other.x && this.y == other.y && this.z == other.z;
  }

  @Override
  public int hashCode() {
    return Objects.hash(x, y, z);
  }
}
