package yangyd.akka.cluster;

public class Main {
  public static void main(String... args) {
    if (args.length == 0) {
      throw new IllegalArgumentException("please specify master or sample");
    }
    switch (args[0]) {
      case "master":
        StatsMaster.startup("master", new String[]{"2551", "2552", "0"});
        break;
      case "sample":
        StatsSample.startup("sample", new String[]{"2551", "2552", "0"});
        break;
      default:
        throw new IllegalArgumentException("please specify master or sample");
    }
  }
}
