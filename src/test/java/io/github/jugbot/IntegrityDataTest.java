package io.github.jugbot;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class IntegrityDataTest {
  @Test
  void opposite() {
    assertTrue(IntegrityData.NORTH.opposite() == IntegrityData.SOUTH);
    assertTrue(IntegrityData.UP.opposite() == IntegrityData.DOWN);
    assertTrue(IntegrityData.EAST.opposite() == IntegrityData.WEST);
    assertTrue(IntegrityData.SOUTH.opposite() == IntegrityData.NORTH);
    assertTrue(IntegrityData.DOWN.opposite() == IntegrityData.UP);
    assertTrue(IntegrityData.WEST.opposite() == IntegrityData.EAST);
    assertTrue(IntegrityData.MASS.opposite() == null);
  }
}
