package mjn.logger.models

final case class TimeSeries(ts: Map[Long, LogGroupStats] = Map.empty) {
  def update(logLine: LogLine): TimeSeries = {
    val timestamp = logLine.date
    ts.get(timestamp) match {
      case Some(stats) =>
        val newStats = stats.update(logLine)
        TimeSeries(ts + (timestamp -> newStats))
      case None =>
        TimeSeries(Map(timestamp -> LogGroupStats(timestamp = timestamp).update(logLine)))
    }
  }
}
