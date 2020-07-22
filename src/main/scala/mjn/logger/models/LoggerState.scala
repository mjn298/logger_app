package mjn.logger.models

import scala.collection.immutable.ListMap

case class LoggerState(alert: Alert = Alert(),
                       stats: ListMap[Long, LogGroupStats] = ListMap.empty,
                       tsMap: TimestampMap = TimestampMap(),
                       summary: Option[LogGroupStats] = None) {

  def update(logLine: LogLine): LoggerState = {
    val (newStats, summaryOpt) = updateStatsMap(logLine)
    val newMap = tsMap.update(logLine)
    LoggerState(
      alert = alert.checkAlert(newMap.provideTotal(logLine.date), logLine.date),
      stats = newStats,
      tsMap = newMap,
      summary = summaryOpt
    )
  }

  def identity: LoggerState = this

  def updateStatsMap(logLine: LogLine): (ListMap[Long, LogGroupStats], Option[LogGroupStats]) = {
    stats.find(_._1 > logLine.date - 10) match {
      case Some((ts, logGroupStats)) => cullMap(stats.updated(ts, logGroupStats.update(logLine)))
      case _ =>
        val newStats = stats + (logLine.date -> LogGroupStats().update(logLine))
        cullMap(newStats)
    }
  }

  private def cullMap(statsMap: ListMap[Long, LogGroupStats]): (ListMap[Long, LogGroupStats], Option[LogGroupStats]) = {
    if (statsMap.size > 2) {
      statsMap.tail -> Some(statsMap.head._2)
    } else {
      statsMap -> None
    }
  }

  def formatSummary(stats: LogGroupStats): String = {
    val topRequest = stats.requestMap.maxBy(_._2)._1
    s"Timestamp: ${stats.timestamp} - Req Count: ${stats.count} - 500s: ${stats.count500} - 404s: ${stats.count404} - Top Req: /$topRequest"
  }
}
