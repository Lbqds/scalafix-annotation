val Scala212 = "2.12.14"
val Scala213 = "2.13.6"

ThisBuild / crossScalaVersions := Seq(Scala213, Scala212)
ThisBuild / scalaVersion := (ThisBuild / crossScalaVersions).value.head

val compilerOptions = Seq(
  "-deprecation",
  "-encoding",
  "UTF-8",
  "-feature",
  "-language:existentials",
  "-language:higherKinds",
  "-unchecked",
  "-Ywarn-dead-code",
  "-Ywarn-numeric-widen",
  "-Yrangepos",
  "-P:semanticdb:synthetics:on"
)

lazy val baseSettings = Seq(
  libraryDependencies ++= Seq(compilerPlugin(scalafixSemanticdb)),
  scalacOptions ++= compilerOptions,
  Compile / console / scalacOptions ~= {
    _.filterNot(Set("-Ywarn-unused-import", "-Ywarn-unused:imports"))
  },
  Test / console / scalacOptions ~= {
    _.filterNot(Set("-Ywarn-unused-import", "-Ywarn-unused:imports"))
  }
)

lazy val V = _root_.scalafix.sbt.BuildInfo

lazy val root = project
  .in(file("."))
  .settings(baseSettings)
  .aggregate(rules, input, output, tests)

lazy val annotations = project
  .in(file("annotations"))
  .settings(baseSettings)

lazy val rules = project
  .settings(baseSettings)
  .settings(
    libraryDependencies += "ch.epfl.scala" %% "scalafix-core" % V.scalafixVersion
  )

lazy val input = project
  .settings(baseSettings)
  .dependsOn(annotations)

lazy val output = project.disablePlugins(ScalafmtPlugin).settings(baseSettings).dependsOn(annotations)

lazy val tests = project
  .settings(baseSettings)
  .settings(
    libraryDependencies += ("ch.epfl.scala" % "scalafix-testkit" % V.scalafixVersion % Test).cross(CrossVersion.full),
    Compile / compile :=
      (Compile / compile).dependsOn(input / Compile / compile).value,
    scalafixTestkitOutputSourceDirectories :=
      (output / Compile / sourceDirectories).value,
    scalafixTestkitInputSourceDirectories :=
      (input / Compile / sourceDirectories).value,
    scalafixTestkitInputClasspath :=
      (input / Compile / fullClasspath).value
  )
  .dependsOn(rules)
  .enablePlugins(ScalafixTestkitPlugin)
