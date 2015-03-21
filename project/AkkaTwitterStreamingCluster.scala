import sbt._
import Keys._
import sbtrelease.ReleasePlugin._
import spray.revolver.RevolverPlugin._

object AkkaTwitterStreamingCluster extends Build {

  object Versions {
    val akka = "2.3.6"
    val spray = "1.3.1"
  }

  val streamingClusterSettings = Seq(
    name := "akka-twitter-streaming-cluster",
    organization := "com.crunchdevelopment",
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
      "Regular spray repo" at "http://repo.spray.io"
    ),

    libraryDependencies ++= Seq(
      "com.typesafe.akka"       %% "akka-contrib"         % Versions.akka,
      "com.typesafe.akka"       %% "akka-actor"           % Versions.akka,
      "com.typesafe.akka"       %% "akka-agent"           % Versions.akka,
      "com.typesafe.akka"       %% "akka-cluster"         % Versions.akka,
      "com.typesafe.akka"       %% "akka-remote"          % Versions.akka,
      "com.typesafe.akka"       %% "akka-slf4j"           % Versions.akka,
      "com.typesafe.akka"       %% "akka-testkit"         % Versions.akka  % "test",

      "com.github.nscala-time"  %% "nscala-time"          % "1.2.0",

      "io.netty"                % "netty-all"             % "4.0.19.Final",

      "io.spray"                %  "spray-can"            % Versions.spray,
      "io.spray"                %  "spray-http"           % Versions.spray,
      "io.spray"                %  "spray-routing"        % Versions.spray,
      "io.spray"                %  "spray-client"         % Versions.spray,
      "io.spray"                % "spray-json"           % Versions.spray,
      "io.spray"                % "spray-testkit"         % Versions.spray % "test",


      "net.liftweb"             %% "lift-json"            % "2.5.1",

      "org.specs2"              %% "specs2"                       % "2.4.2" % "test",
      "org.scalatest"           %% "scalatest"                    % "2.1.3" % "test",
      "org.scalacheck"          %% "scalacheck"                   % "1.11.4" % "test",
      "org.scalamock"           %% "scalamock-scalatest-support"  % "3.0.1" % "test"

    ),

    mainClass in (Compile, packageBin) := Some("com.crunchdevelopment.twitterstreaming.Main"),

    publishArtifact in Test := false,
    publishMavenStyle := true,

    pomIncludeRepository := { x => false },

    bintray.Keys.bintrayOrganization in bintray.Keys.bintray := Some("crunchdevelopment"),


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
          <url>http://alexanderscott.info/</url>
        </developer>
      </developers>

  ) ++ bintray.Plugin.bintrayPublishSettings


  lazy val akkaTwitterStreamingCluster = Project(
    id = "akka-twitter-streaming-cluster",
    base = file("."),
    settings = Project.defaultSettings ++ releaseSettings ++ Revolver.settings ++ streamingClusterSettings
  )
}
