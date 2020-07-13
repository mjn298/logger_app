package mjn.logger.models

case class LoggerState(alert: Alert = Alert(),
                       stats: LogGroupStats = LogGroupStats(),
                       tsMap: TimestampMap = TimestampMap()) {

  def update(logLine: LogLine): LoggerState = {
    val newStats = stats.update(logLine)
    val newMap = tsMap.update(logLine)
    LoggerState(
      alert = alert.checkAlert(newMap.provideAverage(logLine.date), logLine.date),
      stats = newStats,
      tsMap = newMap)
  }
}
