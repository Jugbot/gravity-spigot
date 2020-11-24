package io.github.jugbot;

import org.apache.commons.lang.NotImplementedException;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;

public class MockBlockData implements BlockData {
  final Material material;

  public MockBlockData(Material material) {
    this.material = material;
  }

  @Override
  public BlockData clone() {
    throw new NotImplementedException();
    // return null;
  }

  @Override
  public String getAsString() {
    throw new NotImplementedException();
    // return null;
  }

  @Override
  public String getAsString(boolean arg0) {
    throw new NotImplementedException();
    // return null;
  }

  @Override
  public Material getMaterial() {
    return material;
  }

  @Override
  public boolean matches(BlockData arg0) {
    throw new NotImplementedException();
    // return false;
  }

  @Override
  public BlockData merge(BlockData arg0) {
    throw new NotImplementedException();
    // return null;
  }
}
