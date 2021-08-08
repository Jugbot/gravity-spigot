package io.github.jugbot.gravity;

public class MockConfig extends Config {
  public static Config Instance() {
    if (Config.instance == null) Config.instance = new MockConfig();
    return Config.Instance();
  }
}
