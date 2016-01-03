name := "AsseabetSecurity Core"

version := "2.0.3"

//seq( org.xsbtfilter.SbtFilterPlugin.settings : _*)

//filterEnv <<= selectedProfile.apply(_.get.name)



libraryDependencies ++= Seq(
      "joda-time" % "joda-time" % "2.3",
      //"org.scala-tools.time" % "time_2.9.1" % "0.5",
      "org.slf4s" %% "slf4s-api" % "1.7.7",
        "org.mongodb" %% "casbah-query" % "2.7.4",
  "org.mongodb" %% "casbah-core" % "2.7.4",
  "org.mongodb" %% "casbah-gridfs" % "2.7.4",
  "org.mongodb" %% "casbah-commons" % "2.7.4",
      "ch.qos.logback" % "logback-classic" % "1.0.0",
      "net.liftweb" %% "lift-webkit" % "2.5.1" % "compile->default",
      "net.liftweb" %% "lift-mapper" % "2.5.1" % "compile->default",
      "net.liftweb" %% "lift-wizard" % "2.5.1" % "compile->default",
      "net.liftweb" %% "lift-mongodb" % "2.5.1" % "compile->default",
      "net.liftweb" %% "lift-json-ext" % "2.5.1" % "compile->default",
      "com.typesafe.akka" % "akka-actor_2.10" % "2.1.4",
      "com.typesafe.akka" % "akka-slf4j_2.10" % "2.1.4",
      "com.typesafe.akka" % "akka-remote_2.10" % "2.1.4",
      "com.novus" %% "salat-core" % "1.9.9",
      "org.scalaz" %% "scalaz-core" % "6.0.4",
      "org.scalatest" % "scalatest_2.10" % "1.9.1" % "test",
      "org.apache.commons" % "commons-lang3" % "3.1",
      "commons-net" % "commons-net" % "3.1"
)

libraryDependencies += "org.clapper" %% "argot" % "1.0.3"

libraryDependencies += "com.fasterxml.jackson.core" % "jackson-core" % "2.6.3"

libraryDependencies += "com.fasterxml.jackson.module" % "jackson-module-scala_2.10" % "2.6.3"

libraryDependencies += "com.typesafe.akka" % "akka-testkit_2.10" % "2.1.4"

libraryDependencies += "com.fasterxml.jackson.dataformat" % "jackson-dataformat-xml" % "2.6.3"

libraryDependencies += "com.fasterxml.jackson.datatype" % "jackson-datatype-joda" % "2.6.3"