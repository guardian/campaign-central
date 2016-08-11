addCommandAlias("dist", ";riffRaffArtifact")

import play.sbt.PlayImport.PlayKeys._

name := "campaign-central"

version := "1.0"

resolvers += "Guardian Bintray" at "https://dl.bintray.com/guardian/editorial-tools"

lazy val dependencies = Seq(
  "com.amazonaws" % "aws-java-sdk" % "1.10.33",
  "ai.x" %% "play-json-extensions" % "0.8.0"
)

lazy val root = (project in file(".")).enablePlugins(PlayScala, SbtWeb, RiffRaffArtifact)
  .settings(Defaults.coreDefaultSettings: _*)
  .settings(
    playDefaultPort := 2267,
    riffRaffPackageType := (packageZipTarball in config("universal")).value,
    riffRaffBuildIdentifier := Option(System.getenv("CIRCLE_BUILD_NUM")).getOrElse("DEV"),
    riffRaffUploadArtifactBucket := Option("riffraff-artifact"),
    riffRaffUploadManifestBucket := Option("riffraff-builds"),
    riffRaffPackageName := s"commercial-tools:${name.value}",
    riffRaffManifestProjectName := riffRaffPackageName.value,
    riffRaffArtifactResources := Seq(
      riffRaffPackageType.value -> s"packages/${name.value}/${riffRaffPackageType.value.getName}",
      baseDirectory.value / "deploy.json" -> "deploy.json"
    ),
    scalaVersion := "2.11.8",
    scalaVersion in ThisBuild := "2.11.8",
    libraryDependencies ++= dependencies
  )
