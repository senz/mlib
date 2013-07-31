import io.Source
import sbt._
import Keys._
import play.Project._
import java.io.File

object ApplicationBuild extends Build {
  val appName         = "mlib-example"
  val appVersion = "0.666"

   val standardSettings = Defaults.defaultSettings ++ Seq(
      scalaVersion := "2.10.0"
   )

  // gnu-crypto.jar linked in lib
  val appDependencies = Seq(

    // Add your project dependencies here,
  )

  val mlib = Project("mlib", file("app/mlib"), settings = standardSettings)
  val main = play.Project(appName, appVersion, appDependencies, settings = standardSettings)
    .dependsOn(mlib).aggregate(mlib)
}
