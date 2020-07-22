package mjn.logger.models

import scala.collection.immutable.ListMap

/**
 * LoggerState is the top level state container. Application state is updated with each Log Line.
 * Very strictly, there is no mutation. An entirely new state is created when a line is read.
 * @param alert the current Alert state.
 * @param stats LogGroupStats in ten second intervals.
 * @param tsMap Map of timestamps to requests
 * @param summary A value optionally containing a 10 second summary if certain conditions are met.
 */

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

  def updateStatsMap(logLine: LogLine): (ListMap[Long, LogGroupStats], Option[LogGroupStats]) = {
    stats.find(_._1 > logLine.date - 10) match {
      case Some((ts, logGroupStats)) => cullMap(stats.updated(ts, logGroupStats.update(logLine)))
      case _ =>
        val newStats = stats + (logLine.date -> LogGroupStats().update(logLine))
        cullMap(newStats)
    }
  }

  /**
   *
   * @param statsMap
   * @return I couldn't figure out how to handle the
   */
  private def cullMap(statsMap: ListMap[Long, LogGroupStats]): (ListMap[Long, LogGroupStats], Option[LogGroupStats]) = {
    if (statsMap.size > 2) {
      statsMap.tail -> Some(statsMap.head._2)
    } else {
      statsMap -> None
    }
  }


}
