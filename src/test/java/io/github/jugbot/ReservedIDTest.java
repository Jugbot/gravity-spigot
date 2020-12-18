package io.github.jugbot;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import io.github.jugbot.graph.ReservedID;
import io.github.jugbot.util.Cardinal;

public class ReservedIDTest {
  @Test
  void mappings() {
    assertEquals(ReservedID.SOUTH_DEST, ReservedID.destFrom(Cardinal.SOUTH));
    assertEquals(ReservedID.EAST_DEST, ReservedID.destFrom(Cardinal.EAST));
    assertEquals(ReservedID.NORTH_DEST, ReservedID.destFrom(Cardinal.NORTH));
    assertEquals(ReservedID.SOUTH_DEST, ReservedID.destFrom(Cardinal.SOUTH));
    assertEquals(ReservedID.SOUTH_SOURCE, ReservedID.sourceFrom(Cardinal.SOUTH));
    assertEquals(ReservedID.EAST_SOURCE, ReservedID.sourceFrom(Cardinal.EAST));
    assertEquals(ReservedID.NORTH_SOURCE, ReservedID.sourceFrom(Cardinal.NORTH));
    assertEquals(ReservedID.SOUTH_SOURCE, ReservedID.sourceFrom(Cardinal.SOUTH));
  }
}
