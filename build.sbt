import scala.collection.Seq

val scala3Version = "3.7.0"
val http4sVersion = "1.0.0-M44"
val circeVersion = "0.14.13"

lazy val root = project
  .in(file("."))
  .settings(
    name := "contributors",
    scalaVersion := scala3Version,
    Compile / run / fork := true,
    semanticdbEnabled := true,
    semanticdbVersion := scalafixSemanticdb.revision,
    scalacOptions ++= Seq("-Wunused:imports", "-Wunused:all"),
    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-ember-server",
      "org.http4s" %% "http4s-ember-client",
      "org.http4s" %% "http4s-dsl",
    ).map(_ % http4sVersion),
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio-json" % "0.7.43",
      "dev.zio" %% "zio-prelude" % "1.0.0-RC40",
      "org.typelevel" %% "log4cats-slf4j" % "2.7.0",
      "org.typelevel" %% "munit-cats-effect-3" % "1.0.6" % Test,
      "com.github.pureconfig" %% "pureconfig-core" % "0.17.9",
      "ch.qos.logback" % "logback-classic" % "1.5.18" % Runtime,
    ),
  )
