package io.github.jugbot;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import io.github.jugbot.graph.MaxFlow;
import io.github.jugbot.graph.SubGraph;
import io.github.jugbot.graph.Vertex;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Config.class)
public class SubGraphTest {
  private SubGraph subject;
  private MockWorld mockedWorld;

  @Before
  public void setup() throws Exception {
    Config config = mock(Config.class);
    PowerMockito.whenNew(Config.class).withNoArguments().thenReturn(config);
    when(Config.Instance()).thenReturn(config);
    when(config.getBlockData()).thenReturn(new BlockData());
    MockWorld.HEIGHT = 4;
    mockedWorld = MockWorld.Instance();
    subject = new SubGraph(mockedWorld.getChunkAt(0, 0));
  }

  @Test
  public void maxFlow() {
    Vertex src = new Vertex(mockedWorld.getChunkAt(0, 0), 3);
    Vertex dest = new Vertex(mockedWorld.getChunkAt(0, 0), 4);
    assertEquals(16 * 16 * MockWorld.HEIGHT, MaxFlow.maxFlow(subject, subject.dists, src, dest));
    System.out.println("done");
  }

  public void mockedChunk() throws Exception {
    subject.update(mockedWorld.getChunkAt(0, 0).getChunkSnapshot());
  }
}
