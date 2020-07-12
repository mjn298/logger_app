package mjn.logger.models

case class LoggerState(alert: Alert,
                       stats: LogGroupStats,
                       tsMap: TimestampMap,
                       nextLogLine: LogLine) {

  def update(logLine: LogLine): LoggerState = {
    val newStats = stats.update(nextLogLine)
    val newMap = tsMap.update(nextLogLine)
    LoggerState(
      alert = alert.checkAlert(newMap.provideAverage(nextLogLine.date)),
      stats = newStats,
      tsMap = newMap,
      nextLogLine = logLine)
  }
}
