import java.io.{PrintWriter, FileWriter}
import java.text.SimpleDateFormat
import java.util.{Calendar, Date}
import sbt._
import sbt.Keys._



case class Profile(name: String)

import sbt._


object DSABuild extends Build {
  // setting to make the functionality be accessible from the outside (e.g., the terminal)
  // val selectedProfile = SettingKey[Option[Profile]]("selected-profile", "Uses resources for the specified profile.")


  //lazy val all = Project(id = "all",
//                        base = file("."),
//                        settings = projectSettings("all")) aggregate(core, service)

  lazy val core = Project(id = "core",
                          base = file("core"),
                          settings = projectSettings("core"))

  lazy val rads = Project(id = "rads",
                          base = file("rads"),
                          settings = projectSettings("rads"))

   // lazy val web = Project(id = "web",
//                          base = file("web"),
//                          settings = projectSettings("web")) dependsOn(core % "compile->compile;test->test")

//     lazy val service = Project(id = "service",
//                          base = file("service"),
//                          settings = projectSettings("service")) dependsOn(core % "compile->compile;test->test")


  def projectSettings(id:String) = Defaults.defaultSettings ++ Seq(
    scalacOptions ++= Seq("-unchecked", "-deprecation"),
    parallelExecution in Test := false
  )

}
