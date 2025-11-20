ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.17"

val chiselVersion = "7.3.0"
addCompilerPlugin("org.chipsalliance" % "chisel-plugin" % chiselVersion cross CrossVersion.full)
libraryDependencies += "org.chipsalliance" %% "chisel" % chiselVersion
libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.19" % "test"
libraryDependencies += "nl.tudelft" %% "tydi-chisel" % "0.1.0"
//libraryDependencies += "nl.tudelft" %% "tydi-chisel-test" % "0.1.0" % Test
libraryDependencies += "nl.tudelft" %% "tydi-payload-kit" % "0.1.0-SNAPSHOT" % Test

lazy val root = (project in file("."))
  .settings(
    name := "PostProcessor",
    idePackagePrefix := Some("nl.tudelft.post_processor")
  )
