organization := "com.osinka.play"

name := "play-legitbot"

homepage := Some(url("https://github.com/osinka/play-legitbot"))

startYear := Some(2015)

licenses += "Apache License, Version 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")

organizationName := "Osinka"

description := """Play Framework filter: allow only legitimate search bots to access the site"""

scalaVersion in ThisBuild := "2.11.7"

libraryDependencies in ThisBuild ++= Seq(
  "com.typesafe.play" %% "play" % "2.3.10" % "provided",
  "com.typesafe.play" %% "play-test" % "2.3.10" % "test",
  "org.scalatest" %% "scalatest" % "2.2.5" % "test"
)

resolvers ++= Seq(
  Resolver.typesafeRepo("releases"),
  Resolver.sonatypeRepo("releases")
)

scalacOptions ++= List("-deprecation", "-unchecked", "-feature")

credentials += Credentials(Path.userHome / ".ivy2/credentials_sonatype")

pomIncludeRepository := { x => false }

publishTo <<= (version) { version: String =>
  Some(
    if (version.trim endsWith "SNAPSHOT")
      "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"
    else
      "Sonatype OSS Staging" at "https://oss.sonatype.org/service/local/staging/deploy/maven2/"
  )
}

useGpg := true

pomExtra := <xml:group>
  <developers>
    <developer>
      <id>alaz</id>
      <email>azarov@osinka.com</email>
      <name>Alexander Azarov</name>
      <timezone>+2</timezone>
    </developer>
  </developers>
  <scm>
    <connection>scm:git:git://github.com/osinka/play-legitbot.git</connection>
    <developerConnection>scm:git:git@github.com:osinka/play-legitbot.git</developerConnection>
    <url>http://github.com/osinka/play-legitbot</url>
  </scm>
  <issueManagement>
    <system>github</system>
    <url>http://github.com/osinka/play-legitbot/issues</url>
  </issueManagement>
</xml:group>

