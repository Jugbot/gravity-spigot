package io.github.jugbot.gravity.util;

import java.util.Objects;

public class IntegerXZ {
  public int x, z;

  public IntegerXZ(int x, int z) {
    this.x = x;
    this.z = z;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof IntegerXZ)) return false;
    IntegerXZ other = (IntegerXZ) obj;
    return this.x == other.x && this.z == other.z;
  }

  @Override
  public int hashCode() {
    return Objects.hash(x, z);
  }
}
