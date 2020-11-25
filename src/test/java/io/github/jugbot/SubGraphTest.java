package io.github.jugbot;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import static org.mockito.Mockito.*;
import io.github.jugbot.graph.SubGraph;

import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;



@RunWith(PowerMockRunner.class)
@PrepareForTest(Config.class)
public class SubGraphTest {

  @Test
  public void mockedChunk() throws Exception {
    Config config = mock(Config.class);
    PowerMockito.whenNew(Config.class).withNoArguments().thenReturn(config);
    when(Config.Instance()).thenReturn(config);
    when(config.getBlockData()).thenReturn(new BlockData());
    MockWorld mocked = MockWorld.Instance();
    SubGraph subject = new SubGraph(mocked.getChunkAt(0, 0));
    subject.update(mocked.getChunkAt(0, 0).getChunkSnapshot());
  }
}
