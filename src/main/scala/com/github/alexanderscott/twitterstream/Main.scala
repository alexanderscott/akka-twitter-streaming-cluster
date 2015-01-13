package com.github.alexanderscott.twitterstream

import akka.actor.{PoisonPill, ActorSystem, Props}
import com.github.alexanderscott.twitterstream.core._
import com.typesafe.config._
import akka.cluster.Cluster
import akka.contrib.pattern.{ClusterSingletonProxy, ClusterSingletonManager}
import com.github.alexanderscott.twitterstream.proxy.{TwitterStreamProxySingletonProxy, TwitterStreamProxyManager}

object ClusterRoles {
  val Frontend = "frontend"
  val Backend = "backend"
  val Supervisor = "supervisor"
}


object Main extends App {
  private def startProxyManager(config: Config)(implicit system: ActorSystem): Unit = {
    if (!Cluster(system).getSelfRoles.contains("backend")) {
      return
    }

    system.actorOf(TwitterStreamProxySingletonProxy.props(
      config = config,
      singletonPath = "/user/singleton/stream-proxy",
      role = Some("backend")),
      name = "streamProxy")


    system.actorOf(ClusterSingletonManager.props(
      singletonProps = TwitterStreamProxyManager.props(config),
      singletonName = "proxy-manager",
      terminationMessage = PoisonPill,
      role = Some("backend")),
      name = "proxyManager")
  }

  //def main(args: String*) {
  override def main(args: Array[String]): Unit = {
    println("╔═════════════════════════════════╗")
    println("║ Akka Twitter Streaming Cluster  ║")
    println("╚═════════════════════════════════╝")

    val system = ActorSystem("twitterstreaming")

    val config = ConfigFactory.load()
    if (!config.hasPath("twitterstreaming")){
      throw new Exception("Could not find twitterstreaming config resource")
    }

    val twitterStreamConfig = config.atKey("twitterstreaming")


    //implicit val system = ActorSystem("sys", config)
    //implicit val logger = system.log

    startProxyManager(twitterStreamConfig)

    val streamSupervisor = system.actorOf(TwitterStreamSupervisor.props(twitterStreamConfig))

  }
}
