name := "mlib"

version := "0.1-CURRENT"

scalaVersion := "2.10.0"

resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies += "org.apache.derby" % "derby" % "10.4.1.3"

libraryDependencies += "com.typesafe.akka" % "akka-actor_2.10" % "2.1.4"

libraryDependencies += "com.typesafe.akka" % "akka-testkit_2.10" % "2.1.4"

libraryDependencies += "org.mockito" % "mockito-all" % "1.9.5"

libraryDependencies += "play" % "play_2.10" % "2.1-SNAPSHOT"

libraryDependencies += "play" % "play-iteratees_2.10" % "2.1-SNAPSHOT"

libraryDependencies += "junit" % "junit" % "4.11"
