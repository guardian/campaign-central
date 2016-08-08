addCommandAlias("dist", ";riffRaffArtifact")

name := "campaign-central"

version := "1.0"

lazy val dependencies = Seq(
  "com.amazonaws" % "aws-java-sdk" % "1.10.33"
)

lazy val root = (project in file(".")).enablePlugins(PlayScala, SbtWeb)
  .settings(Defaults.coreDefaultSettings: _*)
  .settings(
    scalaVersion := "2.11.8",
    scalaVersion in ThisBuild := "2.11.8",
    libraryDependencies ++= dependencies
  )
