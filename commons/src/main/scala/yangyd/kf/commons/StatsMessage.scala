package yangyd.kf.commons

object StatsMessage {
  final case class StatsJob(text: String)
  final case class StatsResult(meanWordLength: Double)
  final case class JobFailed(reason: String)
}
