package mjn.logger

import cats.effect.IO

import scala.collection.immutable.ListMap

package object models {
  type StatsMap = IO[ListMap[Long, LogGroupStats]]

}
