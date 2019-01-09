val finchVersion = "0.26.0"
val circeVersion = "0.10.1"
val scalatestVersion = "3.0.5"

lazy val root = (project in file("."))
  .settings(
    organization := "com.nd",
    name := "scheduler-mock-service",
    version := "0.0.1-SNAPSHOT",
    scalaVersion := "2.12.7",
    libraryDependencies ++= Seq(
      "com.github.finagle" %% "finchx-core"  % finchVersion,
      "com.github.finagle" %% "finchx-circe"  % finchVersion,
      "io.circe" %% "circe-generic" % circeVersion,
      "com.outr" %% "hasher" % "1.2.1",

      "org.tpolecat" %% "doobie-core"      % "0.6.0",
      "org.tpolecat" %% "doobie-hikari"    % "0.6.0",          // HikariCP transactor.
      "org.tpolecat" %% "doobie-specs2"    % "0.6.0" % "test", // Specs2 support for typechecking statements.
      "org.tpolecat" %% "doobie-scalatest" % "0.6.0" % "test",  // ScalaTest support for typechecking statements.

      "com.typesafe" % "config" % "1.3.0",

      "mysql" % "mysql-connector-java" % "8.0.11",

      "org.scalatest"      %% "scalatest"    % scalatestVersion % "test"
    )
  )