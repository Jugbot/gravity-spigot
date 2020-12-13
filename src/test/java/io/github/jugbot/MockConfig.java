package io.github.jugbot;

public class MockConfig extends Config {
  public static Config Instance() {
    if (Config.instance == null) Config.instance = new MockConfig();
    return Config.Instance();
  }

  private MockConfig() {
    super.blockData = new IntegrityData();
  }

  @Override
  protected void loadBlockData() {
    return;
  }
}
