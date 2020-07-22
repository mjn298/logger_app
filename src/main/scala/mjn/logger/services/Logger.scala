package mjn.logger.services

import cats.effect.IO
import mjn.logger.models.{LogGroupStats, LogLine, LoggerState}
import java.time._

/**
 * All Side effects are encapsulated here. Everything here either returns an IO Monad, or a formatted string.
 * See "Cats Effect" documentation - https://typelevel.org/cats-effect/
 * Basically, it is a way to declare side effects lazily, preventing their running until they're
 * demanded. In this case, I am using an IO[LoggerState] which can be viewed as a container of,
 * if this were Java, <Void, LoggerState> (Java's void is called "Unit" in scala). It is monadic,
 * therefore composable, and that's how I'm using it here (see call site of updateAndPrintStats in LoggerApp.scala).
 *
 */

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

  def printAlert(loggerState: LoggerState, timestamp: Long): IO[LoggerState] = {
    val alertString =
      if (loggerState.alert.active && loggerState.alert.sendAlert) {
        s"\n---High traffic generated an alert! Hits: ${loggerState.alert.total}, triggered at ${buildDate(loggerState.alert.created.get)}\n"
      } else {
        s"\n---Alert triggered at ${buildDate(loggerState.alert.created.getOrElse(0))} recovered at ${buildDate(timestamp)} with ${loggerState.alert.total} requests\n"
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
