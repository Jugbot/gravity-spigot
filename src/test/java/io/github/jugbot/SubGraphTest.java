package io.github.jugbot;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.logging.Logger;

import com.google.common.graph.Graphs;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import io.github.jugbot.graph.MaxFlow;
import io.github.jugbot.graph.ReservedID;
import io.github.jugbot.graph.SubGraph;
import io.github.jugbot.graph.Vertex;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(PowerMockRunner.class)
@PrepareForTest({Config.class, App.class})
public class SubGraphTest {
  private static SubGraph subject;
  private static MockWorld mockedWorld;

  @Test
  public void step1_setup() throws Exception {
    // PowerMockito.mockStatic(App.class);
    // mock(JavaPlugin.class);
    // App app = mock(App.class);
    // PowerMockito.whenNew(App.class).withNoArguments().thenReturn(app);
    // when(App.getPlugin(App.class)).thenReturn(app);
    // when(app.getLogger()).thenReturn(mock(Logger.class));

    Config config = mock(Config.class);
    PowerMockito.whenNew(Config.class).withNoArguments().thenReturn(config);
    when(Config.Instance()).thenReturn(config);
    when(config.getBlockData()).thenReturn(new BlockData());
    MockWorld.HEIGHT = 4;
    mockedWorld = MockWorld.Instance();
    subject = new SubGraph(mockedWorld.getChunkAt(0, 0));
  }

  @Test
  public void step2_maxFlow() {
    Vertex src = new Vertex(mockedWorld.getChunkAt(0, 0), ReservedID.SOURCE);
    Vertex dest = new Vertex(mockedWorld.getChunkAt(0, 0), ReservedID.DEST);
    assertEquals(16 * 16 * MockWorld.HEIGHT, MaxFlow.maxFlow(subject, subject.dists, src, dest));
    System.out.println("done");
  }

  @Test
  public void step3_mockedChunk() throws Exception {
    // MockChunkSnapshot mockChunk = new MockChunkSnapshot();
    // MockBlock offendingBlock = (MockBlock) mockChunk.blocks[0][128][0];//new MockBlock(new
    // MockBlockData(Material.DIRT), 0, 128, 0);
    // ((MockBlockData)offendingBlock.blockData).material = Material.DIRT;
    Block offending = mockedWorld.getBlockAt(0, 128, 0);
    ((MockBlockData) offending.getBlockData()).material = Material.DIRT;
    subject.update(mockedWorld.getChunkAt(0, 0).getChunkSnapshot());
    assertEquals(1, subject.getIntegrityViolations().length);
    assertEquals(offending, subject.getIntegrityViolations()[0]);
    // Graphs.reachableNodes(subject, new Vertex())
  }
}
