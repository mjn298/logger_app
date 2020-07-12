package mjn.logger.models

final case class Alert(active: Boolean = false, created: Option[Long] = None, threshold: Int = 10) {
  def checkAlert(averageReqs: Double, currTimestamp: Long): Alert = {
    val isExceeded = averageReqs >= threshold
    (active, isExceeded) match {
      case(false, true) => Alert(active = true, created = Some(currTimestamp), threshold)
      case (true, true) => Alert(active, created, threshold)
      case (false, false) => Alert(threshold = threshold)
      case (true, false) => Alert(active = false, created = Some(currTimestamp), threshold)
    }
  }
}
