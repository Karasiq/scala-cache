lazy val commonSettings = Seq(
  scalaVersion := "2.12.3",
  crossScalaVersions := Seq("2.11.11", scalaVersion.value)
)

lazy val core = project
  .settings(
    commonSettings,
    name := "scala-cache-core",
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % "3.0.3" % "test"
    )
  )
  
lazy val simple = project
  .settings(commonSettings, name := "scala-cache-simple")
  .dependsOn(core % "compile->compile;test->test")
  
lazy val larray = project
  .settings(
    commonSettings,
    name := "scala-cache-larray",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor" % "2.5.14" % "provided",
      "org.xerial.larray" %% "larray" % "0.4.0"
    )
  )
  .dependsOn(core % "compile->compile;test->test", simple)

lazy val `scala-cache` = (project in file("."))
  .settings(commonSettings, name := "scala-cache")
  .dependsOn(core, simple, larray)
  .aggregate(core, simple, larray)