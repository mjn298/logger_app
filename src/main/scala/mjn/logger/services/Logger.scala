package mjn.logger.services

import cats.effect.IO
import mjn.logger.models.{LogLine, LoggerState}

// All Side effects are encapsulated here

object Logger {

  def updateAndPrintStats(loggerState: LoggerState, logLine: LogLine): IO[LoggerState] = {
    val newState = loggerState.update(logLine)
    printSummary(newState).flatMap(printAlert(_, logLine.date))
  }

  def printSummary(loggerState: LoggerState): IO[LoggerState] = loggerState.summary match {
    case Some(summary) =>
      IO {
        println(loggerState.formatSummary(summary)); loggerState
      }
    case None => IO(loggerState)
  }

  def printRemainingStats(loggerState: LoggerState) = {
    loggerState.stats.values.map(stats => IO { println(loggerState.formatSummary(stats)) })
  }

  def printAlert(loggerState: LoggerState, timestamp: Long): IO[LoggerState] = {
    val alertString =
      if (loggerState.alert.active && loggerState.alert.sendAlert) {
        s"---High traffic generated an alert! Hits: ${loggerState.alert.total}, triggered at ${loggerState.alert.created.get}"
      } else {
        s"---Alert triggered at ${loggerState.alert.created.getOrElse(0)} recovered at $timestamp."
      }
    if (loggerState.alert.sendAlert) IO {
      println(alertString); loggerState
    }
    else IO(loggerState)
  }
}
