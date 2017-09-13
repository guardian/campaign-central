addCommandAlias("dist", ";riffRaffArtifact")

import play.sbt.PlayImport.PlayKeys._

name := "campaign-central"

version := "1.0"

resolvers += "Guardian Bintray" at "https://dl.bintray.com/guardian/editorial-tools"

val slf4jVersion    = "1.7.25"
val playJsonVersion = "2.6.2"

lazy val dependencies = Seq(
  "com.typesafe.play"                %% "play-json"                             % playJsonVersion,
  "com.typesafe.play"                %% "play-json-joda"                        % playJsonVersion,
  "ai.x"                             %% "play-json-extensions"                  % "0.10.0",
  "com.fasterxml.jackson.dataformat" % "jackson-dataformat-cbor"                % "2.8.9",
  "com.amazonaws"                    % "aws-java-sdk"                           % "1.11.170",
  "com.gu"                           %% "play-googleauth"                       % "0.7.0",
  "com.gu"                           %% "content-api-client"                    % "11.19",
  "com.google.apis"                  % "google-api-services-analyticsreporting" % "v4-rev115-1.22.0",
  "com.squareup.okhttp3"             % "okhttp"                                 % "3.4.1",
  ws,
  "commons-io"           % "commons-io"               % "2.5",
  "net.logstash.logback" % "logstash-logback-encoder" % "4.11",
  "com.gu"               % "kinesis-logback-appender" % "1.4.0",
  "org.slf4j"            % "slf4j-api"                % slf4jVersion,
  "org.slf4j"            % "jcl-over-slf4j"           % slf4jVersion,
  "org.scalatest"        %% "scalatest"               % "3.0.3" % Test
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
    debianPackageDependencies := Seq("openjdk-8-jre-headless"),
    maintainer := "Commercial Dev Team <commercial.dev@theguardian.com>",
    packageSummary := description.value,
    packageDescription := description.value,
    playDefaultPort := 2267,
    packageName in Universal := normalizedName.value,
    name in Universal := normalizedName.value,
    topLevelDirectory in Universal := Some(normalizedName.value),
    riffRaffPackageType := (packageBin in Debian).value,
    riffRaffBuildIdentifier := Option(System.getenv("CIRCLE_BUILD_NUM")).getOrElse("DEV"),
    riffRaffUploadArtifactBucket := Option("riffraff-artifact"),
    riffRaffUploadManifestBucket := Option("riffraff-builds"),
    riffRaffPackageName := s"commercial-tools:${name.value}",
    riffRaffManifestProjectName := riffRaffPackageName.value,
    riffRaffArtifactResources := Seq(
      riffRaffPackageType.value              -> s"${name.value}/${riffRaffPackageType.value.getName}",
      baseDirectory.value / "riff-raff.yaml" -> "riff-raff.yaml"
    ),
    scalaVersion := "2.11.8",
    scalaVersion in ThisBuild := "2.11.8",
    libraryDependencies ++= dependencies,
    // this can be removed when it's no longer a transitive dependency
    excludeDependencies += "com.gu" %% "pan-domain-auth-play_2-5",
    scalafmtOnCompile := true
  )
