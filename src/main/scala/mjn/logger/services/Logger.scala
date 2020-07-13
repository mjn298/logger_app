package mjn.logger.services

import mjn.logger.models.{Alert, LogGroupStats, LogLine, LoggerState}

import scala.collection.mutable

// All Side effects/mutable state are encapsulated here


/*
Idea for maintaining the ten second summaries -
It appears that messages do not come in more than a couple of seconds out of order, so
will build up, from the start, the stats for seconds 0-9 and 10-19 and then bump the head
from the queue. At the end, we'll just print whatever we have. Can also keep track of
"lastPrintedSummaryTimetamp" but I really want to figure out a way to handle all this mutable state.

 */
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
