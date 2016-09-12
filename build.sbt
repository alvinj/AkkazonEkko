name := "Ekko"

version := "0.4"

scalaVersion := "2.11.7"

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies ++= Seq(
    "com.typesafe.akka" %% "akka-actor" % "2.4.9",
    "org.scala-lang.modules" %% "scala-xml" % "1.0.3"
)

scalacOptions += "-deprecation"


