package mjn.logger.models

case class TimestampMap(map: Map[Long, Int] = Map.empty, timeRange: Int = 120) {

  def update(nextLogLine: LogLine): TimestampMap = {
    /*
    This culling occurs to keep the timestamp map at a reasonable size,
    since we don't care about anything before curr - 2 minutes
     */
    val cullThreshold = timeRange + 10
    val incrementedCount: Int =  map.getOrElse(nextLogLine.date, 0) + 1
    if(map.size > cullThreshold) {
      val timestampsToRemove = map.keys.filter(_ < nextLogLine.date - cullThreshold)
      TimestampMap(
        map = (map -- timestampsToRemove) + (nextLogLine.date -> incrementedCount),
        timeRange = timeRange)
    }
    else {
      TimestampMap(
        map = map + (nextLogLine.date -> incrementedCount),
        timeRange = timeRange
      )
    }
  }

  def provideTotal(currTimestamp: Long): Int =
    map.filter(_._1 > currTimestamp - timeRange).foldLeft(0){_ + _._2}
}
