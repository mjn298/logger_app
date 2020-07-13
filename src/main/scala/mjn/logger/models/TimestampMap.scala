package mjn.logger.models

case class TimestampMap(map: Map[Long, Int] = Map.empty, timeRange: Int = 120) {

  def update(nextLogLine: LogLine): TimestampMap = {
    val cullThreshold = timeRange + 10
    val timestampsToRemove = map.keys.filter(_ <= nextLogLine.date - cullThreshold)
    val incrementedCount: Int =  map.getOrElse(nextLogLine.date, 0) + 1
    TimestampMap(
      map = (map -- timestampsToRemove) + (nextLogLine.date -> incrementedCount),
      timeRange = timeRange)
  }

  def provideAverage(currTimestamp: Long) : Double =
    map.filter(_._1 >= currTimestamp - timeRange).foldLeft(0.0){_ + _._2} / timeRange

}
