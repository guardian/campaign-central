addCommandAlias("dist", ";riffRaffArtifact")

import play.sbt.PlayImport.PlayKeys._

name := "campaign-central"

version := "1.0"

resolvers += "Guardian Bintray" at "https://dl.bintray.com/guardian/editorial-tools"
resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

val slf4jVersion    = "1.7.25"
val playJsonVersion = "2.6.7"

lazy val dependencies = Seq(
  ws,
  "com.typesafe.play"                %% "play-json"               % playJsonVersion,
  "com.typesafe.play"                %% "play-json-joda"          % playJsonVersion,
  "org.typelevel"                    %% "cats-core"               % "0.9.0",
  "ai.x"                             %% "play-json-extensions"    % "0.10.0",
  "com.fasterxml.jackson.dataformat" % "jackson-dataformat-cbor"  % "2.8.9" % Runtime,
  "com.amazonaws"                    % "aws-java-sdk"             % "1.11.248",
  "com.gu"                           %% "play-googleauth"         % "0.7.0",
  "com.gu"                           %% "content-api-client"      % "11.45",
  "com.gu"                           %% "content-api-client-aws"  % "0.5",
  "com.squareup.okhttp3"             % "okhttp"                   % "3.9.1",
  "commons-io"                       % "commons-io"               % "2.6",
  "net.logstash.logback"             % "logstash-logback-encoder" % "4.11",
  "com.gu"                           % "kinesis-logback-appender" % "1.4.2",
  "org.slf4j"                        % "slf4j-api"                % slf4jVersion,
  "org.slf4j"                        % "jcl-over-slf4j"           % slf4jVersion,
  "com.gu"                           %% "scanamo"                 % "0.9.5",
  "org.scalacheck"                   %% "scalacheck"              % "1.13.5" % Test
)

lazy val root = (project in file("."))
  .enablePlugins(PlayScala, SbtWeb, RiffRaffArtifact, JDebPackaging, SystemdPlugin)
  .settings(Defaults.coreDefaultSettings: _*)
  .settings(
    javaOptions in Universal ++= Seq(
      "-Dpidfile.path=/dev/null",
      "-J-XX:MaxRAMFraction=2",
      "-J-XX:InitialRAMFraction=2",
      "-J-XX:MaxMetaspaceSize=500m",
      "-J-XX:+UseConcMarkSweepGC",
      "-J-XX:+PrintGCDetails",
      "-J-XX:+PrintGCDateStamps",
      s"-J-Xloggc:/var/log/${packageName.value}/gc.log"
    ),
    scalacOptions := Seq(
      "-Ypartial-unification"
    ),
    debianPackageDependencies := Seq("openjdk-8-jre-headless"),
    maintainer := "Commercial Dev Team <commercial.dev@theguardian.com>",
    packageSummary := description.value,
    packageDescription := description.value,
    playDefaultPort := 2267,
    packageName in Universal := normalizedName.value,
    name in Universal := normalizedName.value,
    topLevelDirectory in Universal := Some(normalizedName.value),
    riffRaffPackageType := (packageBin in Debian).value,
    riffRaffUploadArtifactBucket := Option("riffraff-artifact"),
    riffRaffUploadManifestBucket := Option("riffraff-builds"),
    riffRaffPackageName := s"commercial-tools:${name.value}",
    riffRaffManifestProjectName := riffRaffPackageName.value,
    riffRaffArtifactResources := Seq(
      riffRaffPackageType.value              -> s"${name.value}/${riffRaffPackageType.value.getName}",
      baseDirectory.value / "riff-raff.yaml" -> "riff-raff.yaml"
    ),
    scalaVersion := "2.12.4",
    libraryDependencies ++= dependencies,
    scalafmtOnCompile := true
  )
