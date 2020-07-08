package mjn.logger.models

final case class Alert(active: Boolean = false, lastUpdated: Option[Long] = None) {
  def checkAlert(logGroupStats: LogGroupStats): Alert = ???
}
