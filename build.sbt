name := "AkkaAssignment"

version := "0.1"

scalaVersion := "2.13.6"
lazy val akkaVersion = "2.5.23"

//lazy val akkaVersion = "2.6.15"
lazy val cassandraVersion = "0.101"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
  "com.typesafe.akka" %% "akka-persistence" % akkaVersion,
  "org.scalatest" %% "scalatest" % "3.2.9",
  //Cassandra dependencies
  "com.typesafe.akka" %% "akka-persistence-cassandra" % cassandraVersion,
  "com.typesafe.akka" %% "akka-persistence-cassandra-launcher" % cassandraVersion % Test
//  "au.com.bytecode" % "opencsv" % "2.4"
)