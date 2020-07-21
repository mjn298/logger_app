package mjn.logger

import java.nio.file.Path

import cats._
import cats.implicits._
import cats.effect._
import fs2.Stream
import fs2.io._
import fs2.text
import fs2._
import info.fingo.spata.{CSVParser, CSVRecord, Maybe}
import info.fingo.spata.io._
import mjn.logger.models.{LogLine, LoggerState}

import scala.io.Source
import scala.concurrent.ExecutionContext.Implicits.global
import scala.io.Source

object LoggerApp extends IOApp {
  def run(args: List[String]): IO[ExitCode] = {
    records.compile.drain.unsafeRunSync()

    IO[ExitCode](ExitCode.Success)
  }

  implicit val cs = IO.contextShift(global)
  val parser: CSVParser[IO] = CSVParser[IO]()
  val records: Stream[IO, (LoggerState, Maybe[LogLine])] = Stream
    .bracket(IO { Source.fromResource("sample_csv.txt") })(source => IO { source.close() })
    .through(reader[IO].by)
    .through(parser.parse)
    .map(_.to[LogLine]())
    .evalMapAccumulate(LoggerState())((loggerState, row) => {
      val nextState = row match {
        case Right(logLine) => loggerState.update(logLine)
        case _ => loggerState.identity
      }
      val printTotal = Applicative[IO].whenA(true)(IO{println(loggerState.getTotal)})
//      val printIf5 = Applicative[IO].whenA(ctr == 0)(IO(println(row.toString)))
//      val action = printIf5 >> IO({}).as(nextCtr -> row)
//      action
      IO().as(nextState -> row)
    })


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
