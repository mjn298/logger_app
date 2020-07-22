package mjn.logger.models

final case class Alert(active: Boolean = false, created: Option[Long] = None, threshold: Int = 10, total: Int = 0, sendAlert: Boolean = false) {

  private def getAvg(totalReqs: Int): Double = totalReqs / 120.0

  def alertRecovered(totalReqs: Int, currTimestamp: Long, range: Int): Boolean = {
    val notExceeded = getAvg(totalReqs) < threshold
    val enoughTimeElapsed = created match {
      case Some(ts) => currTimestamp - ts >= range
      case None => false
    }
    notExceeded && enoughTimeElapsed
  }

  def checkAlert(totalReqs: Int, currTimestamp: Long, range: Int = 120): Alert = {
    val isExceeded = getAvg(totalReqs) >= threshold
    val recovered = alertRecovered(totalReqs, currTimestamp, range)
    (active, isExceeded) match {
      case (false, true) => Alert(active = true, created = Some(currTimestamp), threshold, totalReqs, sendAlert = true)
      case (true, true) => Alert(active, created, threshold, totalReqs)
      case (false, false) => Alert(threshold = threshold, created = None, total = totalReqs)
      case (true, false) => Alert(active = !recovered, created, threshold, totalReqs, sendAlert = recovered)
    }
  }
}
