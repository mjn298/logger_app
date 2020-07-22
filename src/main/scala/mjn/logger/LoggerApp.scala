package mjn.logger

import cats.effect._
import fs2.Stream
import info.fingo.spata.io._
import info.fingo.spata.{CSVParser, Maybe}
import mjn.logger.models.{LogLine, LoggerState}
import mjn.logger.services.Logger

import scala.concurrent.ExecutionContext.Implicits.global
import scala.io.Source

object LoggerApp extends IOApp {
  def run(args: List[String]): IO[ExitCode] =
    records.compile.drain.as(ExitCode.Success)

  implicit val cs = IO.contextShift(global)
  val parser: CSVParser[IO] = CSVParser[IO]()
  val records: Stream[IO, (LoggerState, Maybe[LogLine])] = Stream
    .bracket(IO {
      Source.fromResource("sample_csv.txt")
    })(source => IO {
      source.close()
    })
    .through(reader[IO].by)
    .through(parser.parse)
    .map(_.to[LogLine]())
    .evalMapAccumulate(LoggerState())((loggerState, row) => {
      val nextState = row match {
        case Right(logLine) => Logger.updateAndPrintStats(loggerState, logLine)
        case _ => IO(loggerState)
      }
      nextState.map(_ -> row)
    })
}
