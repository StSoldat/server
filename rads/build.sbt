name := "Realtime Analytics DataService"

version := "0.0.1"

scalaVersion := "2.10.4"

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

//libraryDependencies += "javax.servlet" % "javax.servlet-api" % "3.0.1"

libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.3.6"

libraryDependencies += "org.apache.spark" %% "spark-core" % "1.5.1"

libraryDependencies += "org.apache.spark" % "spark-mllib_2.10" % "1.5.1"

//csv parser
libraryDependencies += "com.github.tototoshi" %% "scala-csv" % "1.1.2"