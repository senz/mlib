name := "mlib"

version := "0.1-CURRENT"

scalaVersion := "2.10.0"

resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies += "play" %% "play-iteratees" % "2.1.1"

libraryDependencies += "play" %% "play" % "2.1.1"

libraryDependencies += "play" %% "play-test" % "2.1.3"

libraryDependencies += "com.typesafe.akka" % "akka-actor_2.10" % "2.1.4"

libraryDependencies += "com.typesafe.akka" % "akka-testkit_2.10" % "2.1.4"

libraryDependencies += "org.mockito" % "mockito-all" % "1.9.5"

libraryDependencies += "junit" % "junit" % "4.11"

libraryDependencies += "joda-time" % "joda-time" % "2.3"
