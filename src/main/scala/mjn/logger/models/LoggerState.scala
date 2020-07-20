package mjn.logger.models

import scala.collection.immutable.ListMap

case class LoggerState(alert: Alert = Alert(),
                       stats: ListMap[Long, LogGroupStats] = ListMap.empty,
                       tsMap: TimestampMap = TimestampMap()) {

  def update(logLine: LogLine): LoggerState = {
    val newStats = updateStatsMap(logLine)
    val newMap = tsMap.update(logLine)
    LoggerState(
      alert = alert.checkAlert(newMap.provideAverage(logLine.date), logLine.date),
      stats = getSummaryToPrint(newStats),
      tsMap = newMap)
  }

  def identity: LoggerState = this

  def updateStatsMap(logLine: LogLine): ListMap[Long, LogGroupStats] = {
    stats.find(_._1 > logLine.date - 10) match {
      case Some((ts, logGroupStats)) => stats.updated(ts, logGroupStats.update(logLine))
      case _ => stats + (logLine.date -> LogGroupStats().update(logLine))
    }
  }

  def getTotal: Int = stats.values.foldLeft(0)(_ + _.count)

  def getSummaryToPrint(statsMap: ListMap[Long, LogGroupStats]): ListMap[Long, LogGroupStats] = {
    if (stats.size > 2) {
      println(statsMap.head)
      statsMap.tail
    } else {
      statsMap
    }
  }
}
