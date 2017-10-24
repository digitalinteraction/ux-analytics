name := "ux-analytics"

version := "0.1"

scalaVersion := "2.12.3"

libraryDependencies += "org.postgresql" % "postgresql" % "42.1.4"

libraryDependencies += "com.typesafe" % "config" % "1.3.1"

libraryDependencies += "com.zaxxer" % "HikariCP" % "2.7.2"

libraryDependencies += "com.typesafe.play" %% "anorm" % "2.5.3"

val circeVersion = "0.8.0"

libraryDependencies ++= Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser"
).map(_ % circeVersion)
        