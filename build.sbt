name := "spray-playground"

scalacOptions ++= Seq(
  "-g:vars",
  "-deprecation",
  "-unchecked",
  "-feature",
  "-Xlint")

scalaVersion := "2.11.6"


val akkaVersion = "2.3.9"
val sprayVersion = "1.3.1"


libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "io.spray" %% "spray-can" % sprayVersion,
  "io.spray" %% "spray-http" % sprayVersion,
  "io.spray" %% "spray-routing" % sprayVersion,
  "org.scalatest" %% "scalatest" % "2.2.4" % "test")


Revolver.settings

