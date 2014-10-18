package com.github.alexanderscott.twitterstream

import akka.actor.{ActorSystem, Props}
import com.github.alexanderscott.twitterstream.core._
import scala.annotation.tailrec

object ClusterRoles {
  val Frontend = "frontend"
  val Backend = "backend"
  val Supervisor = "supervisor"
}

object TwitterStreamApp extends App {
  import com.github.alexanderscott.twitterstream.oauth._
  //import com.github.alexanderscott.twitterstream.Commands._

  def main(args: String*) {
    val system = ActorSystem("twitterstreaming")

    val streamBackend = system.actorOf(Props(classOf[TweetStreamBackendActor]), ClusterRoles.Backend)
    
    val streamSupervisor = system.actorOf(Props(classOf[TweetStreamSupervisor]), ClusterRoles.Supervisor)

    val streamFrontend = system.actorOf(Props(classOf[TweetStreamFrontendActor]), ClusterRoles.Frontend)

  }
}
