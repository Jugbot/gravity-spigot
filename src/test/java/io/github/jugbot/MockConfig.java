package io.github.jugbot;

public class MockConfig extends Config {
  public static Config Instance() {
    if (Config.instance == null) Config.instance = new MockConfig();
    return Config.Instance();
  }
}
