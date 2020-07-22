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
   * @return A tuple of a maybe updated stats map, and an Optional ten second summary. There is a bug here.
   *         In inspecting the data, I saw that requests might come in out of order, and I can't guarantee that
   *         all ten seconds of requests will come in before the 11th second does - so, I allow a maximum of two sets of 10 sec summaries.
   *         When the third one starts (ie, n0 + 20), I pop the Summ0 off the queue, and indicate that it should be printed.
   *         You will see the consequence of this decision - the final two 10 second summaries are not printed in the output.
   *         I haven't yet figured out how to "flush state" after processing an FS2 stream - that's the next thing I'll explore.
   *         In a way, the output is "eventually" consistent with the input, and with an infinite stream there would be no "end state" to flush.
   */
  private def cullMap(statsMap: ListMap[Long, LogGroupStats]): (ListMap[Long, LogGroupStats], Option[LogGroupStats]) = {
    if (statsMap.size > 2) {
      statsMap.tail -> Some(statsMap.head._2)
    } else {
      statsMap -> None
    }
  }


}
