organization  := "com.gu"
description   := "AWS Lambda for initiating the refreshing of campaigns in Campaign Central"
scalaVersion  := "2.12.2"
scalacOptions ++= Seq("-deprecation", "-feature", "-unchecked", "-target:jvm-1.8", "-Xfatal-warnings")
name := "refresh-campaigns"

val AwsSdkVersion = "1.11.118"

libraryDependencies ++= Seq(
  "com.amazonaws" % "aws-java-sdk-lambda" % AwsSdkVersion,
  "com.squareup.okhttp3" % "okhttp" % "3.6.0"
)

initialize := {
  val _ = initialize.value
  assert(sys.props("java.specification.version") == "1.8",
    "Java 8 is required for this project.")
}
