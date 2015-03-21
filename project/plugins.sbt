// fast turnaround / restart app
addSbtPlugin("io.spray" % "sbt-revolver" % "0.7.2")

// Release management
addSbtPlugin("com.github.gseitz" % "sbt-release" % "0.8.3")

// Show a full dependency graph
addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.7.4")

resolvers += Classpaths.sbtPluginReleases

// Publish to BinTray
resolvers += Resolver.url(
  "bintray-sbt-plugin-releases",
  url("http://dl.bintray.com/content/sbt/sbt-plugin-releases"))(
    Resolver.ivyStylePatterns)

addSbtPlugin("me.lessis" % "bintray-sbt" % "0.1.2")

