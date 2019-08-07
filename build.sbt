lazy val dependenciesManager = new {
    val scalaTestVersion = "3.0.8"
    val logBackVersion = "1.2.3"
    val scalaLoggingVersion = "3.9.2"
    val catsVersion = "2.0.0-M4"
    val py4jVersion = "0.10.8.1"
    val jepVersion = "3.8.2"

    val scalaTest = "org.scalatest" %% "scalatest" % scalaTestVersion % Test
    val logback = "ch.qos.logback" % "logback-classic" % logBackVersion
    val scalaLogging = "com.typesafe.scala-logging" %% "scala-logging" % scalaLoggingVersion
    val cats = "org.typelevel" %% "cats-core" % catsVersion
    val cats_effect = "org.typelevel" %% "cats-effect" % catsVersion
    val py4j = "net.sf.py4j" % "py4j" % py4jVersion
    val jep = "black.ninia" % "jep" % jepVersion
}

lazy val dependencies = Seq(
    dependenciesManager.scalaTest,
    dependenciesManager.logback,
    dependenciesManager.scalaLogging,
    dependenciesManager.cats,
    dependenciesManager.cats_effect,
    dependenciesManager.py4j,
    dependenciesManager.jep
)

lazy val global = project
        .in(file("."))
        .aggregate(
            Scala,
            Python
        )

lazy val Scala = project
        .settings(
            name := "Scala",
            version := "0.1",
            scalaVersion := "2.13.0",
            libraryDependencies ++= dependencies,
            scalacOptions ++= Seq(
                "-language:higherKinds",
                "-deprecation",
                "-encoding", "UTF-8",
                "-feature",
                "-language:_"
            ),
            scalaSource in Compile := baseDirectory.value / "main/scala",
            scalaSource in Test := baseDirectory.value / "test/scala",
            ideaInternalPlugins := Seq("properties"),
            javaOptions += "-Djava.library.path=C:\\Users\\337541502\\AppData\\Roaming\\Python\\Python37\\site-packages\\jep"
        )//.enablePlugins(SbtIdeaPlugin)

lazy val Python = (project in file("Python"))
                .settings(
                    scalaSource in Compile := baseDirectory.value / "main/python"
                )
