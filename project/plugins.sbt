sbtVersion := "0.13.7"

scalaVersion := "2.10.4"

javacOptions ++= Seq("-source", "1.7", "target", "1.7")

resolvers += "maven2" at "http://repo1.maven.org/maven2"

resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.6.0")


