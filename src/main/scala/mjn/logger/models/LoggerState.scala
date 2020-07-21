package mjn.logger.models

import cats.effect.IO

import scala.collection.immutable.ListMap

case class LoggerState(alert: Alert = Alert(),
                       stats: ListMap[Long, LogGroupStats] = ListMap.empty,
                       tsMap: TimestampMap = TimestampMap()
                       ) {

  def update(logLine: LogLine): LoggerState = {
    val newStats = updateStatsMap(logLine)
    val newMap = tsMap.update(logLine)
    LoggerState(
      alert = alert.checkAlert(newMap.provideAverage(logLine.date), logLine.date),
      stats = newStats,
      tsMap = newMap
    )
  }

  def identity: LoggerState = this

  def updateStatsMap(logLine: LogLine): ListMap[Long, LogGroupStats] = {
    stats.find(_._1 > logLine.date - 10) match {
      case Some((ts, logGroupStats)) => stats.updated(ts, logGroupStats.update(logLine))
      case _ => stats + (logLine.date -> LogGroupStats().update(logLine))
    }
  }

//  def getTotal: IO[Int] = stats.map( _.values.foldLeft(0)(_ + _.count))

  private def printSummary(stats: ListMap[Long, LogGroupStats]): IO[Unit] =
    if(stats.size > 2) IO { println(stats.head) }
    else IO.unit

  private def cullMap(statsMap: ListMap[Long, LogGroupStats]): ListMap[Long, LogGroupStats] = {
    if (statsMap.size > 2) {
      statsMap.tail
    } else {
      statsMap
    }
  }
  
  def cullAndPrint: IO[ListMap[Long, LogGroupStats]] = for {
    n <- IO(cullMap(stats))
    _ <- printSummary(stats)
  } yield n

}
