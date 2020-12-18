package io.github.jugbot;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class IntegrityDataTest {
  @Test
  void opposite() {
    assertTrue(Integrity.NORTH.opposite() == Integrity.SOUTH);
    assertTrue(Integrity.UP.opposite() == Integrity.DOWN);
    assertTrue(Integrity.EAST.opposite() == Integrity.WEST);
    assertTrue(Integrity.SOUTH.opposite() == Integrity.NORTH);
    assertTrue(Integrity.DOWN.opposite() == Integrity.UP);
    assertTrue(Integrity.WEST.opposite() == Integrity.EAST);
    assertTrue(Integrity.MASS.opposite() == null);
  }
}
