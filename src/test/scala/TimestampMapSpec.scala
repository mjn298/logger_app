import mjn.logger.models.{LogLine, TimestampMap}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

import scala.util.Random

class TimestampMapSpec extends AnyFlatSpec with should.Matchers{
  private def buildMap(randMin: Int, randMax: Int, start: Int, size: Int) = {
    val map = (start.toLong until start.toLong + size.toLong).map(_ -> Random.between(randMin, randMax)).toMap
    val total = map.values.sum
    (map, total)
  }

  "Timestamp Map" should "update itself without culling when under the threshold" in {
    val (map, total) = buildMap(0, 20, 0, 100)
    val tsMap = TimestampMap(map = map)
    val logLine: LogLine = LogLine("Test", "Test", 101L, "Test", "Test", 1)
    val newMap = tsMap.update(logLine)
    newMap.provideTotal(101L) should be (total + 1)
    newMap.map.size should be (101)
  }

  "Timestamp Map" should "cull when length exceeds threshold + buffer" in {
//    val start = 0L
    val threshold = 120
    val buffer = 10
    val newTimestamp = 131L
    val thresholdAndBuffer = threshold + buffer

    val (map, _) = buildMap(0 , 10, 0, thresholdAndBuffer)

    val tsMap = TimestampMap(map = map)
    val oldThresholdTotal = tsMap.provideTotal(130L)
    val earliestCount: Int = map(0L)
    val oldMapTotal = tsMap.map.values.sum

    val logLine: LogLine = LogLine("Test", "Test", newTimestamp, "Test", "Test", 1)

    val newMap = tsMap.update(logLine)
    val newMapTotal = newMap.map.values.sum
    val newThresholdTotal = newMap.provideTotal(newTimestamp)
    val firstTimeOut = newMap.map(newTimestamp - threshold - 1)


    newThresholdTotal should be ((oldThresholdTotal + 1 - firstTimeOut))
    newMapTotal should be (oldMapTotal - earliestCount + 1)
  }

}
