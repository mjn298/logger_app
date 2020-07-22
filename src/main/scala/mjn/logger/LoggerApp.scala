package mjn.logger

import cats.effect._
import fs2.Stream
import info.fingo.spata.io._
import info.fingo.spata.{CSVParser, Maybe}
import mjn.logger.models.{Alert, LogLine, LoggerState}
import mjn.logger.services.Logger

import scala.concurrent.ExecutionContext.Implicits.global
import scala.io.Source

object LoggerApp extends IOApp {

  def run(args: List[String]): IO[ExitCode] = {
    val threshold = args.headOption match {
      case Some(s) => s.toInt
      case None => 10
    }
    records(threshold).compile.drain.as(ExitCode.Success)
  }

  implicit val cs = IO.contextShift(global)
  val parser: CSVParser[IO] = CSVParser[IO]()
  def records(threshold: Int) : Stream[IO, (LoggerState, Maybe[LogLine])] = {
    val initialAlert = Alert(threshold = threshold)
    Stream
      .bracket(
        IO {Source.fromResource("sample_csv.txt")})(source => IO {source.close()})
      .through(reader[IO].by)
      .through(parser.parse)
      .map(_.to[LogLine]())
      .evalMapAccumulate(LoggerState(alert = initialAlert))((loggerState, row) => {
        val nextState = row match {
          case Right(logLine) => Logger.updateAndPrintStats(loggerState, logLine)
          case _ => IO(loggerState)
        }
        nextState.map(_ -> row)
      })
  }
}
