package mjn.logger.models

final case class LogGroupStats(count: Int = 0,
                               count404: Int = 0,
                               count500: Int = 0,
                               avgSize: Double = 0.0,
                               timestamp: Long = 0L,
                               requestMap: Map[String, Int] = Map.empty
                               ) {
  def update(logLine: LogLine): LogGroupStats = {
    val incrementedCount = requestMap.getOrElse(logLine.request, 0) + 1

    LogGroupStats(
      count = count + 1,
      count404 = if (logLine.status == "404") count404 + 1 else count404,
      count500 = if (logLine.status == "500") count500 + 1 else count500,
      avgSize = avgSize + ((logLine.bytes - avgSize) / (count + 1)),
      timestamp =  if (timestamp == 0L) logLine.date else timestamp,
      requestMap = requestMap + (logLine.request -> incrementedCount))
  }

  def mostFrequent: String = requestMap.toSeq.maxBy(_._2)._1
}