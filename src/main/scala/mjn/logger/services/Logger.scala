package mjn.logger.services

import cats.effect.IO
import mjn.logger.models.{LogGroupStats, LogLine, LoggerState}
import java.time._

// All Side effects are encapsulated here

object Logger {

  def updateAndPrintStats(loggerState: LoggerState, logLine: LogLine): IO[LoggerState] = {
    val newState = loggerState.update(logLine)
    printSummary(newState).flatMap(printAlert(_, logLine.date))
  }

  def printSummary(loggerState: LoggerState): IO[LoggerState] = loggerState.summary match {
    case Some(summary) =>
      IO {
        println(formatSummary(summary)); loggerState
      }
    case None => IO(loggerState)
  }

  def printRemainingStats(loggerState: LoggerState) = {
    loggerState.stats.values.map(stats => IO { println(formatSummary(stats)) })
  }

  def printAlert(loggerState: LoggerState, timestamp: Long): IO[LoggerState] = {
    val alertString =
      if (loggerState.alert.active && loggerState.alert.sendAlert) {
        s"\n---High traffic generated an alert! Hits: ${loggerState.alert.total}, triggered at ${buildDate(loggerState.alert.created.get)}\n"
      } else {
        s"\n---Alert triggered at ${buildDate(loggerState.alert.created.getOrElse(0))} recovered at ${buildDate(timestamp)}\n"
      }
    if (loggerState.alert.sendAlert) IO {
      println(alertString); loggerState
    }
    else IO(loggerState)
  }

  def buildDate(timestamp: Long) = {
   Instant.ofEpochSecond(timestamp).atZone(ZoneId.of("Z"))
  }

  def formatSummary(stats: LogGroupStats): String = {
    val topRequest = stats.requestMap.maxBy(_._2)._1
    s"Timestamp: ${buildDate(stats.timestamp)} - Average Size: ${stats.avgSize.formatted("%.2f")} - Req Count: ${stats.count} - 500s: ${stats.count500} - 404s: ${stats.count404} - Top Req: /$topRequest"
  }
}
