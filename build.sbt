name := "logger_app"

version := "0.1"

scalaVersion := "2.13.3"

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-core" % "2.1.1",
  "org.typelevel" %% "cats-effect" % "2.1.4",
  "co.fs2" %% "fs2-core" % "2.4.2",
  "co.fs2" %% "fs2-io" % "2.4.0"
)



