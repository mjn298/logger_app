package mjn.logger.services

import mjn.logger.models.{Alert, LogGroupStats, LogLine, LoggerState}

import scala.collection.mutable

// All Side effects/mutable state are encapsulated here

class Logger(firstLogLine: LogLine) {

  private val summaryIncrement = 10
  private val summaryTolerance = 5

  private var appState: LoggerState = LoggerState(nextLogLine = firstLogLine)

  private val startTimestamp = firstLogLine.date

  private val statsMap = mutable.Map.empty[Long, LogGroupStats]

  def getStatsFromMap(timestamp: Long): LogGroupStats = {
    statsMap.find(_._1 + summaryIncrement <= timestamp)

  }

  def update(logLine: LogLine): LoggerState = {
    appState = appState.update(logLine)
    if ((logLine.date - firstLogLine.date) % summaryIncrement == 0)

  }



}
