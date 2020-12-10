package io.github.jugbot;

import static org.mockito.Mockito.mockStatic;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import org.bukkit.Material;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import io.github.jugbot.graph.ReservedID;
import io.github.jugbot.graph.SubGraph;
import io.github.jugbot.graph.SuperGraph;
import io.github.jugbot.graph.Vertex;

public class SuperGraphTest {
  private SuperGraph subject = new SuperGraph();
  private Table<Integer, Integer, SubGraph> subgraphs = HashBasedTable.create();
  private MockWorld mockedWorld;
  private static MockedStatic<Config> mocked = mockStatic(Config.class);

  @BeforeEach
  public void setup() throws Exception {
    IntegrityData idc = new IntegrityData();
    mocked.when(() -> Config.getStructuralData(Material.AIR)).thenReturn(idc.getEmpty());
    mocked.when(() -> Config.getStructuralData(Material.DIRT)).thenReturn(idc.getDefault());
    mocked.when(() -> Config.isStructural(Material.AIR)).thenReturn(false);
    mocked.when(() -> Config.isStructural(Material.DIRT)).thenReturn(true);
    MockWorld.HEIGHT = 4;
    mockedWorld = MockWorld.Instance();
    subgraphs.put(0, 0, new SubGraph(mockedWorld.getChunkAt(0, 0)));
    subgraphs.put(1, 0, new SubGraph(mockedWorld.getChunkAt(1, 0)));
  }

  @Nested
  class TwoChunks {
    @Test
    public void add() {
      subject.add(subgraphs.get(0, 0));
      Vertex s = new Vertex(ReservedID.SUPER_SOURCE);
      Vertex t = new Vertex(ReservedID.SUPER_DEST);
      Utils.verifyGraph(subject, s, t);
    }
  }
}
