lazy val commonSettings = Seq(
  scalaVersion := "2.12.3",
  crossScalaVersions := Seq("2.11.11", scalaVersion.value)
)

lazy val publishSettings = Seq(
  publishMavenStyle := true,
  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (isSnapshot.value)
      Some("snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("releases" at nexus + "service/local/staging/deploy/maven2")
  },
  publishArtifact in Test := false,
  pomIncludeRepository := { _ ⇒ false },
  licenses := Seq("Apache License, Version 2.0" → url("http://opensource.org/licenses/Apache-2.0")),
  homepage := Some(url("https://github.com/Karasiq/scala-cache")),
  pomExtra := <scm>
    <url>git@github.com:Karasiq/scala-cache.git</url>
    <connection>scm:git:git@github.com:Karasiq/scala-cache.git</connection>
  </scm>
    <developers>
      <developer>
        <id>karasiq</id>
        <name>Piston Karasiq</name>
        <url>https://github.com/Karasiq</url>
      </developer>
    </developers>
)

lazy val noPublishSettings = Seq(
  publishArtifact := false,
  publishArtifact in makePom := false,
  publishTo := Some(Resolver.file("Repo", file("target/repo")))
)

lazy val releaseSettings = Seq(
  releaseCrossBuild := true,
  releasePublishArtifactsAction := PgpKeys.publishSigned.value,
  releaseProcess := {
    import ReleaseTransformations._

    Seq[ReleaseStep](
      checkSnapshotDependencies,
      inquireVersions,
      runClean,
      runTest,
      setReleaseVersion,
      publishArtifacts, // releaseStepCommand("+publishSigned"),
      releaseStepCommand("sonatypeRelease"),
      commitReleaseVersion,
      tagRelease,
      setNextVersion,
      commitNextVersion,
      pushChanges
    )
  }
)

lazy val core = crossProject.crossType(CrossType.Pure)
  .settings(
    commonSettings,
    publishSettings,
    name := "scala-cache-core",
    libraryDependencies ++= Seq("org.scalatest" %%% "scalatest" % "3.0.3" % "test")
  )

lazy val coreJS = core.js

lazy val coreJVM = core.jvm
  
lazy val simple = crossProject.crossType(CrossType.Pure)
  .settings(commonSettings, publishSettings, name := "scala-cache-simple")
  .dependsOn(core % "compile->compile;test->test")

lazy val simpleJS = simple.js

lazy val simpleJVM = simple.jvm
  
lazy val larray = project
  .settings(
    commonSettings,
    publishSettings,
    name := "scala-cache-larray",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor" % "2.5.14" % "provided",
      "org.xerial.larray" %% "larray" % "0.4.0"
    )
  )
  .dependsOn(coreJVM % "compile->compile;test->test", simpleJVM)

lazy val `scala-cache` = (project in file("."))
  .settings(commonSettings, publishSettings, releaseSettings, name := "scala-cache")
  .dependsOn(coreJVM, simpleJVM, larray)
  .aggregate(coreJS, coreJVM, simpleJS, simpleJVM, larray)