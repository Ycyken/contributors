import scala.collection.Seq

val scala3Version = "3.7.0"
val Http4sVersion = "1.0.0-M44"

lazy val root = project
  .in(file("."))
  .settings(
    name := "contributors",

    scalaVersion := scala3Version,

    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-ember-server" % Http4sVersion,
      "org.http4s" %% "http4s-ember-client" % Http4sVersion,
      "org.http4s" %% "http4s-dsl" % Http4sVersion,
      "org.typelevel" %% "log4cats-slf4j" % "2.6.0",
      "com.typesafe.play" %% "play-json" % "2.10.6",
      "org.typelevel" %% "munit-cats-effect-3" % "1.0.6" % Test,
      "com.github.pureconfig" %% "pureconfig-core" % "0.17.9",
      "ch.qos.logback" % "logback-classic" % "1.5.18" % Runtime,
    ),
  )
