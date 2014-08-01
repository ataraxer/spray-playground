name := "spray-playground"

version := "0.1.0-SNAPSHOT"

organization := "com.ataraxer"

scalaVersion := "2.10.4"

scalacOptions ++= Seq(
  "-g:vars",
  "-deprecation",
  "-unchecked",
  "-feature",
  "-Xlint",
  "-Xfatal-warnings")

/* ==== DEPENDENCIES ==== */
libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor"   % "2.3.3",
  "io.spray" % "spray-can" % "1.3.1",
  "io.spray" % "spray-http" % "1.3.1",
  "io.spray" % "spray-routing" % "1.3.1",
  "org.scalatest" %% "scalatest" % "2.1.0" % "test",
  "log4j" % "log4j" % "1.2.15" excludeAll (
    ExclusionRule(organization = "com.sun.jdmk"),
    ExclusionRule(organization = "com.sun.jmx"),
    ExclusionRule(organization = "javax.jmx")),
  "org.slf4j" % "slf4j-log4j12" % "1.7.5"
    exclude("org.slf4j", "slf4j-simple"))

Revolver.settings

mainClass in Revolver.reStart := Some("com.ataraxer.sprayer.REST")

/* ==== OTHER ==== */
testOptions in Test += Tests.Setup { classLoader =>
  classLoader
  .loadClass("org.slf4j.LoggerFactory")
  .getMethod("getLogger", classLoader.loadClass("java.lang.String"))
  .invoke(null, "ROOT")
}

