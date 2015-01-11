import sbt._
import Keys._
import sbtrelease.ReleasePlugin._
import spray.revolver.RevolverPlugin._

object AkkaTwitterStreamingCluster extends Build {

  val streamingClusterSettings = Seq(
    name := "akka-twitter-streaming-cluster",
    organization := "com.github.alexanderscott",
    crossScalaVersions := Seq("2.10.4", "2.11.2"),

    parallelExecution in Test := false,

    scalacOptions ++= Seq(
      "-unchecked",
      "-deprecation",
      "-Xlint",
      "-encoding", "UTF-8"
    ),

    resolvers ++= Seq(
      "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
      "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
      "Sonatype Releases" at "https://oss.sonatype.org/content/repositories/releases",
      "Regular spray repo" at "http://repo.spray.io",
      "Nightly spray repo" at "http://nightlies.spray.io"
    ),

    libraryDependencies ++= Seq(
      "com.typesafe.akka"       %% "akka-contrib"         % "2.3.4",
      "com.typesafe.akka"       %% "akka-actor"           % "2.3.4",
      "com.typesafe.akka"       %% "akka-agent"           % "2.3.4",
      "com.typesafe.akka"       %% "akka-cluster"         % "2.3.4",
      "com.typesafe.akka"       %% "akka-remote"          % "2.3.4",
      "com.typesafe.akka"       %% "akka-slf4j"           % "2.3.4",
      "com.typesafe.akka"       %% "akka-testkit"         % "2.3.4"  % "test",

      "io.netty"                % "netty-all"             % "4.0.19.Final",

      "io.spray"                %  "spray-can"            % "1.3.1",
      "io.spray"                %  "spray-http"           % "1.3.1",
      "io.spray"                %  "spray-routing"        % "1.3.1",
      "io.spray"                %  "spray-client"         % "1.3.1",
      "io.spray"                %% "spray-json"           % "1.2.6",
      "io.spray"                % "spray-testkit"         % "1.3.1" % "test",

      "org.scalacheck"          %% "scalacheck"                   % "1.11.4" % "test",
      "org.scalamock"           %% "scalamock-scalatest-support"  % "3.0.1" % "test"

    ),

    mainClass in (Compile, packageBin) := Some("com.github.alexanderscott.twitterstream.Main"),

    publishArtifact in Test := false,
    publishMavenStyle := true,

    pomIncludeRepository := { x => false },

    pomExtra :=
      <url>https://github.com/alexanderscott/akka-twitter-streaming-cluster</url>
      <licenses>
        <license>
          <name>Apache 2</name>
          <url>http://www.apache.org/licenses/LICENSE-2.0.txt</url>
          <distribution>repo</distribution>
        </license>
      </licenses>
      <scm>
        <url>git@github.com:alexanderscott/akka-twitter-streaming-cluster.git</url>
        <connection>scm:git:git@github.com:alexanderscott/akka-twitter-streaming-cluster.git</connection>
      </scm>
      <developers>
        <developer>
          <id>alexanderscott</id>
          <name>Alex Ehrnschwender</name>
          <url>https://alexehrnschwender.com/</url>
        </developer>
      </developers>

  )

  lazy val akkaTwitterStreamingCluster = Project(
    id = "akka-twitter-streaming-cluster",
    base = file("."),
    settings = Project.defaultSettings ++ releaseSettings ++ Revolver.settings ++ streamingClusterSettings
  )
}
