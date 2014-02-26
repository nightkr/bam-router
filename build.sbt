val akkaVersion = "2.2.3"
val json4sVersion = "3.2.7"

scalaVersion := "2.10.3"

resolvers += "spray" at "http://repo.spray.io/"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-kernel" % akkaVersion,
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
  "org.json4s" %% "json4s-jackson" % json4sVersion,
  "ch.qos.logback"    % "logback-classic" % "1.0.0"
)

scalacOptions += "-feature"

distSettings

atmosSettings
