package mjn.logger

import java.nio.file.Path

import cats.effect.{ExitCode, IO, IOApp}
import fs2.Stream
import info.fingo.spata.io.reader

object LoggerApp extends IOApp {
  def run(args: List[String]): IO[ExitCode] = {
    IO[ExitCode](ExitCode.Success)
  }

  val stream: Stream[IO, Char] = reader.plain[IO].read(Path.of("sample_csv.txt"))
  stream.compile.
  //  def main(args: Array[String]): Unit = {
  //    val bufferedSource = getClass.getResourceAsStream("/sample_csv.txt")
  //    val lines = scala.io.Source.fromInputStream(bufferedSource)
  //  }
  //
  //  def readLogLine(line: String): LogLine = {
  //    val cols = line.split(",").map(_.trim)
  //    LogLine(cols(0), cols(2), cols(3).toLong, cols(4), cols(5), cols(6).toInt)
  //  }
  //
  //  @tailrec
  //  def process(lines: LazyList[String], state: LoggerState): LogGroupStats = {
  //    if(lines.isEmpty) state.stats
  //    else {
  //      process(lines.tail, state.update(readLogLine(lines.head)))
  //    }
  //  }
}
