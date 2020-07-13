package mjn.logger

import mjn.logger.models.{LogGroupStats, LogLine, LoggerState}

import scala.annotation.tailrec

object LoggerApp {
  def main(args: Array[String]): Unit = {
    val bufferedSource = getClass.getResourceAsStream("/sample_csv.txt")
    val lines = scala.io.Source.fromInputStream(bufferedSource).getLines.
    process(lines.getLines., LoggerState())
  }

  def readLogLine(line: String): LogLine = {
    val cols = line.split(",").map(_.trim)
    LogLine(cols(0), cols(2), cols(3).toLong, cols(4), cols(5), cols(6).toInt)
  }

  @tailrec
  def process(lines: LazyList[String], state: LoggerState): LogGroupStats = {
    if(lines.isEmpty) state.stats
    else {
      process(lines.tail, state.update(readLogLine(lines.head)))
    }
  }
}
