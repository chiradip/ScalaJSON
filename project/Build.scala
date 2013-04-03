import sbt._
import Keys._
 
object ScalaJSONBuild extends Build {
  val Organization = "Chiradip Mandal, Inc."
  val Version      = "0.1"
  val ScalaVersion = "2.10.1"
 
  lazy val ScalaJSON = Project(
    id = "scala-json",
    base = file("."),
    settings = defaultSettings ++ Seq(
      libraryDependencies ++= Dependencies.scalaJSON))
 
  lazy val buildSettings = Defaults.defaultSettings ++ Seq(
    organization := Organization,
    version      := Version,
    scalaVersion := ScalaVersion,
    crossPaths   := false,
    organizationName := "Chiradip Mandal Inc.",
    organizationHomepage := Some(url("http://www.chiradip.com"))
  )
  
  lazy val defaultSettings = buildSettings ++ Seq(
    resolvers += "Typesafe Release Repo" at "http://repo.typesafe.com/typesafe/releases/",
    resolvers += "Typesafe Snapshot Repo" at "http://repo.typesafe.com/typesafe/snapshots/",
 
    // compile options
    scalacOptions ++= Seq("-encoding", "UTF-8", "-deprecation", "-unchecked"),
    javacOptions  ++= Seq("-Xlint:unchecked", "-Xlint:deprecation")
 
  )
}
 
