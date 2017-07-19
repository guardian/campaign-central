addCommandAlias("dist", ";riffRaffArtifact")

import play.sbt.PlayImport.PlayKeys._

name := "campaign-central"

version := "1.0"

resolvers += "Guardian Bintray" at "https://dl.bintray.com/guardian/editorial-tools"

val slf4jVersion = "1.7.25"
val playJsonVersion = "2.6.2"
val dfpClientVersion = "3.5.0"

lazy val dependencies = Seq(
  "com.typesafe.play" %% "play-json" % playJsonVersion,
  "com.typesafe.play" %% "play-json-joda" % playJsonVersion,
  "ai.x" %% "play-json-extensions" % "0.10.0",
  "com.amazonaws" % "aws-java-sdk" % "1.11.8",
  "com.gu" %% "pan-domain-auth-play_2-6" % "0.5.0",
  "com.gu" %% "panda-hmac" % "1.2.0",
  "com.gu" %% "content-api-client" % "11.19",
  "com.google.apis" % "google-api-services-analyticsreporting" % "v4-rev115-1.22.0",
  "com.squareup.okhttp3" % "okhttp" % "3.4.1",
  ws,
  "commons-io" % "commons-io" % "2.5",
  "net.logstash.logback" % "logstash-logback-encoder" % "4.7",
  "com.gu" % "kinesis-logback-appender" % "1.3.0",
  "org.slf4j" % "slf4j-api" % slf4jVersion,
  "org.slf4j" % "jcl-over-slf4j" % slf4jVersion,
  "com.google.api-ads" % "ads-lib" % dfpClientVersion,
  "com.google.api-ads" % "dfp-axis" % dfpClientVersion,
  "com.google.guava" % "guava" % "20.0",
  "org.scalatest" %% "scalatest" % "3.0.3" % Test
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
    libraryDependencies ++= dependencies,
    // this can be removed when it's no longer a transitive dependency
    excludeDependencies += "com.gu" %% "pan-domain-auth-play_2-5"
  )
