package mjn.logger.models
import java.text.SimpleDateFormat

final case class LogLine(remotehost: String,
                         authuser: String,
                         date: Long,
                         request: String,
                         status: String,
                         bytes: Int) {
  def getSection: String = request.split("/")(1).split(" ")(0)
  def getVerb: String = request.split(" ")(0)
  def getSeconds: Int = new SimpleDateFormat("ss").format(date).toInt
  def getMinutes: Int = new SimpleDateFormat("mm").format(date).toInt
}
