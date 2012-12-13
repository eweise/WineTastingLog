import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

  val appName = "winetaste"
  val appVersion = "1.0"

  val appDependencies = Seq(
    // Add your project dependencies here,
  )

  val main = PlayProject(appName, appVersion, appDependencies, mainLang = SCALA).settings(

    libraryDependencies ++= Seq("postgresql" % "postgresql" % "9.1-901-1.jdbc4" , "commons-io" % "commons-io" % "2.1")


  )

}
