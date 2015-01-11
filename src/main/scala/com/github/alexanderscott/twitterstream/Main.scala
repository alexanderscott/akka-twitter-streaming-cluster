package com.github.alexanderscott.twitterstream

import akka.actor.{ActorSystem, Props}
import com.github.alexanderscott.twitterstream.core._
import com.typesafe.config._

object ClusterRoles {
  val Frontend = "frontend"
  val Backend = "backend"
  val Supervisor = "supervisor"
}

object Main extends App {

  def main(args: String*) {
    val system = ActorSystem("twitterstreaming")

    val config = ConfigFactory.load("twitterstreaming")

    if (!config.hasPath("twitterstreaming")){
      throw new Exception("Could not find twitterstreaming config resource")
    }

    val streamSupervisor = system.actorOf(TwitterStreamSupervisor.props(config))

  }
}
