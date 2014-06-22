import sbt._
import sbt.Keys._

import com.typesafe.sbt.{SbtSite, SbtGhPages}
import com.typesafe.sbt.SbtGhPages.GhPagesKeys._
import com.typesafe.sbt.SbtGit.GitKeys.gitRemoteRepo

import scoverage._


object SprayPlaygroundBuild extends Build {

  val akkaVersion = "2.3.3"
  val sprayVersion = "1.3.1"
  val scalatestVersion = "2.1.0"

  lazy val commonSettings = Seq(
    scalacOptions ++= Seq(
      "-g:vars",
      "-deprecation",
      "-unchecked",
      "-feature",
      "-Xlint",
      "-Xfatal-warnings"
    ),

    resolvers ++= Seq(
      "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
      "Spray Repository" at "http://repo.spray.io"
    ),

    libraryDependencies ++= Seq(
      // Akka
      "com.typesafe.akka" %% "akka-actor"   % akkaVersion,
      "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
      "com.typesafe.akka" %% "akka-slf4j"   % akkaVersion,
      // Spray
      "io.spray" % "spray-can"  % sprayVersion,
      "io.spray" % "spray-http" % sprayVersion,
      "io.spray" % "spray-routing" % sprayVersion,
      // Slick
      "com.typesafe.slick" %% "slick" % "2.0.2",
      // ScalaTest
      "org.scalatest" %% "scalatest" % scalatestVersion % "test",
      // Logging
      "log4j" % "log4j" % "1.2.15" excludeAll (
        ExclusionRule(organization = "com.sun.jdmk"),
        ExclusionRule(organization = "com.sun.jmx"),
        ExclusionRule(organization = "javax.jms")
      ),
      "org.slf4j" % "slf4j-log4j12" % "1.7.5"
        exclude("org.slf4j", "slf4j-simple")
    ),

    gitRemoteRepo := "git@github.com:ataraxer/spray-patterns.git",

    parallelExecution := true
  )


  lazy val buildSettings =
    Defaults.defaultSettings ++
    SbtSite.site.settings ++
    SbtSite.site.includeScaladoc() ++
    SbtGhPages.ghpages.settings ++
    ScoverageSbtPlugin.instrumentSettings ++
    Seq(
      name         := "spray-patterns",
      version      := "0.1.0",
      scalaVersion := "2.10.3"
    )

  lazy val akkit = Project(
    id = "spray-patterns",
    base = file("."),
    settings = buildSettings
  ).settings(
    commonSettings: _*
  )

}


// vim: set ts=2 sw=2 et:
