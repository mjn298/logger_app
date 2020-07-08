package mjn.logger.services

import mjn.logger.models.{Alert, LogGroupStats}

// I don't have to mutate anything at all, just have methods that accept what's
// current and return the new state

class Logger {
  type Summary = (LogGroupStats, String)
  private def read = ???
  private def appendResource(resources: Map[String, LogGroupStats], resource: LogGroupStats): Map[String, LogGroupStats] = ???
  private def buildTimeSeries(ts: Map[Long, Int]): Map[Long, Int] = ???
  private def getAlerts(alerts: List[Alert]): List[Alert] = ???
  private def bumpFromTimeSeries(ts: Map[Long, Int], time: Long): Map[Long, Int] = ???
  private def generateAlert(ts: Map[Long, Int], time: Long): Option[Alert] = ???
  private def provideSummary(stats: List[LogGroupStats]): List[_] = ???



}
