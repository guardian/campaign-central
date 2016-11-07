addCommandAlias("dist", ";riffRaffArtifact")

import play.sbt.PlayImport.PlayKeys._

name := "campaign-central"

version := "1.0"

resolvers += "Guardian Bintray" at "https://dl.bintray.com/guardian/editorial-tools"

val slf4jVersion = "1.7.21"

lazy val dependencies = Seq(
  "com.amazonaws" % "aws-java-sdk" % "1.11.8",
  "ai.x" %% "play-json-extensions" % "0.8.0",
  "com.gu" % "pan-domain-auth-play_2-5_2.11" % "0.4.0",
  "com.gu" %% "content-api-client" % "10.5",
  "com.google.apis" % "google-api-services-analyticsreporting" % "v4-rev10-1.22.0",
  "com.squareup.okhttp3" % "okhttp" % "3.4.1",
  ws,
  "net.logstash.logback" % "logstash-logback-encoder" % "4.7",
  "com.gu" % "kinesis-logback-appender" % "1.3.0",
  "org.slf4j" % "slf4j-api" % slf4jVersion,
  "org.slf4j" % "jcl-over-slf4j" % slf4jVersion,
  "org.joda" % "joda-convert" % "1.8.1",
  "com.google.api-ads" % "dfp-axis" % "2.20.0",
  "org.scalatest" %% "scalatest" % "3.0.0" % Test
)

lazy val root = (project in file(".")).enablePlugins(PlayScala, SbtWeb, RiffRaffArtifact)
  .settings(Defaults.coreDefaultSettings: _*)
  .settings(
    playDefaultPort := 2267,
    packageName in Universal := normalizedName.value,
    name in Universal := normalizedName.value,
    topLevelDirectory in Universal := Some(normalizedName.value),
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
