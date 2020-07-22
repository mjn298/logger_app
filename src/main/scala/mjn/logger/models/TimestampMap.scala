package mjn.logger.models

case class TimestampMap(map: Map[Long, Int] = Map.empty, timeRange: Int = 120) {

  def update(nextLogLine: LogLine): TimestampMap = {
    /*
    This culling occurs to keep the timestamp map at a reasonable size,
    since we don't care about anything before curr - 2 minutes
     */
    val cullThreshold = timeRange + 10
    val incrementedCount: Int =  map.getOrElse(nextLogLine.date, 0) + 1
    val newMap = map + (nextLogLine.date -> incrementedCount)
    if(newMap.size > cullThreshold) {
      val timestampsToRemove = newMap.keys.filter(_ < nextLogLine.date - cullThreshold.toLong)
      TimestampMap(
        map = newMap -- timestampsToRemove,
        timeRange = timeRange)
    }
    else {
      TimestampMap(
        map = newMap,
        timeRange = timeRange
      )
    }
  }

  def provideTotal(currTimestamp: Long): Int =
    map.filter(_._1 >= currTimestamp - timeRange).foldLeft(0){_ + _._2}
}
