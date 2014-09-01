package com.github.alexanderscott.twitterstream

import akka.actor.{ActorSystem, Props}
import com.github.alexanderscott.twitterstream.oauth.OAuthTwitterAuthorization
import com.github.alexanderscott.twitterstream.core._
import scala.annotation.tailrec

object ClusterRoles {
  val Frontend = "frontend"
  val Backend = "backend"
  val Supervisor = "supervisor"
}

object TwitterStreamApp extends App {
  import com.github.alexanderscott.twitterstream.oauth._
  import com.github.alexanderscott.twitterstream.Commands._


  override def main(args: String*) {
    val system = ActorSystem("twitterstreaming")

    val streamBackend = system.actorOf(Props(new TweetStreamBackendActor(TweetStreamBackendActor.twitterUri) with OAuthTwitterAuthorization), ClusterRoles.Backend)
    
    val streamSupervisor = system.actorOf(Props(new TweetStreamSupervisor, ClusterRoles.Supervisor)

    val streamFrontend = system.actorOf(Props(new TweetStreamFrontendActor, ClusterRoles.Frontend)

  }
}
