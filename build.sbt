name := "ThrottlingService"

version := "1.0"

lazy val `throttlingservice` = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq( jdbc , cache , ws   , specs2 % Test )

unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )  

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"

libraryDependencies += "com.typesafe.akka" %% "akka-camel" % "2.4.1"

libraryDependencies += "org.apache.activemq" % "activemq-camel" % "5.13.3"

libraryDependencies += "org.apache.activemq" % "activemq-all" % "5.13.3"