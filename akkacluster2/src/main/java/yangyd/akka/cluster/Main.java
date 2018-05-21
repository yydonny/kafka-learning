package yangyd.akka.cluster;

public class Main {
  public static void main(String... args) {
    if (args.length == 0) {
      throw new IllegalArgumentException("client or proxy");
    }
    switch (args[0]) {
      case "client":
        StatsClient.startup();
        break;
      case "proxy":
        StatsProxy.startup();
      default:
        throw new IllegalArgumentException("client or proxy");
    }
  }
}
