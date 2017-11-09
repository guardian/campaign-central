// The Typesafe repository
resolvers += "Typesafe repository" at "https://repo.typesafe.com/typesafe/maven-releases/"

// Use the Play sbt plugin for Play projects
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.6.6")

addSbtPlugin("com.typesafe.sbt" % "sbt-web" % "1.4.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-digest" % "1.1.3")

addSbtPlugin("com.typesafe.sbt" % "sbt-gzip" % "1.0.2")

addSbtPlugin("com.gu" % "sbt-riffraff-artifact" % "1.1.3")

addSbtPlugin("io.get-coursier" % "sbt-coursier" % "1.0.0-RC13")

addSbtPlugin("com.lucidchart" % "sbt-scalafmt" % "1.12")

libraryDependencies += "org.vafer" % "jdeb" % "1.3" artifacts (Artifact("jdeb", "jar", "jar"))
