import mjn.logger.models.Alert
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

import scala.util.Random

class AlertSpec extends AnyFlatSpec with should.Matchers{

  private def buildMap(randMin: Int, randMax: Int, start: Int, size: Int) = {
    val map = (start.toLong until start + size).map(_ -> Random.between(randMin, randMax)).toMap
    val total = map.values.sum
    (map, total)
  }

  "An Alert" should "be inactive when under the threshold" in {
    val mapSize = 120
    val (_, mapTotal) = buildMap(0, 10, 1, 120)
    val alert = Alert()
    val updatedAlert = alert.checkAlert(mapTotal, mapSize, mapSize)
    updatedAlert.active should be (false)
    updatedAlert.created should be (None)
    updatedAlert.total should be (mapTotal)
    updatedAlert.sendAlert should be (false)
  }

  "An alert" should "become active when threshold is exceeded" in {
    val mapSize = 120
    val (_, mapTotal) = buildMap(10, 50, 1, mapSize)
    val alert = Alert()
    val updatedAlert = alert.checkAlert(mapTotal, mapSize, mapSize)
    updatedAlert.active should be (true)
    updatedAlert.created should be (Some(120))
    updatedAlert.total should be (mapTotal)
    updatedAlert.sendAlert should be (true)
  }

  "An alert" should "not send an alert when already active" in {
    val mapSize = 120
    val (_, mapTotal) = buildMap(10, 50, 1, mapSize)
    val alert = Alert(active = true, created = Some(5), total = mapTotal, sendAlert = true)
    val updatedAlert = alert.checkAlert(mapTotal + 100, 6, mapSize)
    updatedAlert.active should be (true)
    updatedAlert.created should be (Some(5))
    updatedAlert.total should be (mapTotal + 100)
    updatedAlert.sendAlert should be (false)
  }

  "An alert" should "exit alert state and enter notify state once average is under threshold" in {
    val mapSize = 120
    val (_, mapTotal) = buildMap(0, 10, 120, 240)
    val alert = Alert(active = true, created = Some(120), total = 500)
    val updatedAlert = alert.checkAlert(mapTotal, 240, mapSize)
    updatedAlert.active should be (false)
    updatedAlert.created should be (Some(120))
    updatedAlert.total should be (mapTotal)
    updatedAlert.sendAlert should be (true)
  }
}
