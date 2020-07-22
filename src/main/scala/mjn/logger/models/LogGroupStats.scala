package mjn.logger.models

import cats._
import cats.implicits._

final case class LogGroupStats(count: Int = 0,
                               count404: Int = 0,
                               count500: Int = 0,
                               avgSize: Double = 0.0,
                               timestamp: Long = 0L,
                               requestMap: Map[String, Int] = Map.empty
                              ) {
  def update(logLine: LogLine): LogGroupStats = {
    val incrementedCount = requestMap.getOrElse(formatRequest(logLine.request), 0) + 1
    LogGroupStats(
      count = count + 1,
      count404 = if (logLine.status == "404") count404 + 1 else count404,
      count500 = if (logLine.status == "500") count500 + 1 else count500,
      avgSize = avgSize + ((logLine.bytes - avgSize) / (count + 1)),
      timestamp = if (timestamp == 0L) logLine.date else timestamp,
      requestMap = requestMap + (formatRequest(logLine.request) -> incrementedCount))
  }

  def formatRequest(input: String): String = {
    input.split(" ")(1).split("/")(1)
  }

  def mostFrequent: String = requestMap.toSeq.maxBy(_._2)._1
}

object LogGroupStats {
  implicit val logGroupStatsMonoid: Monoid[LogGroupStats] = new Monoid[LogGroupStats] {
    def combine(x: LogGroupStats, y: LogGroupStats): LogGroupStats = {
      val count = x.count + y.count
      val count404 = x.count404 + y.count404
      val count500 = x.count500 + y.count500
      val avgSize = (x.avgSize * x.count) |+| (y.avgSize * y.count) / count
      val timestamp = x.timestamp
      val requestMap = x.requestMap |+| y.requestMap
      LogGroupStats(count, count404, count500, avgSize, timestamp, requestMap)
    }

    def empty: LogGroupStats = LogGroupStats()
  }
}

