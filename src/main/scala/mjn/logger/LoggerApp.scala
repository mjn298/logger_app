package mjn.logger

import mjn.logger.models.LogLine

object LoggerApp {
  def main(args: Array[String]): Unit = {
    val bufferedSource = getClass.getResourceAsStream("/sample_csv.txt")
    val lines = scala.io.Source.fromInputStream(bufferedSource)
    var ct = 0
    for (line <- lines.getLines) {
      val cols = line.split(",").map(_.trim)
      if(ct != 0) {
        val logLine = LogLine(cols(0), cols(2), cols(3).toLong, cols(4), cols(5), cols(6).toInt)
        println(logLine.toString)
      }
      ct += 1
    }
  }
}
