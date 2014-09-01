package com.github.alexanderscott.twitterstream.core

import akka.actor.{ActorRef, Actor, Props}
import spray.http._
import spray.can.Http
import spray.http.HttpRequest

object TweetStreamSupervisor {
  val twitterUri = Uri("https://stream.twitter.com/1.1/statuses/filter.json")
  def props(): Props = Props(classOf[TweetStreamSupervisor](twitterUri))
}

class TweetStreamSupervisor extends Actor with ActorLogging {

  val streamBackend = system.actorOf(Props(new TweetStreamBackendActor(TweetStreamBackendActor.twitterUri) with OAuthTwitterAuthorization), ClusterRoles.Backend)

  context.watch(streamBackend)

  def receive: Receive = {
    case Terminated: (ref: ActorRef) => {
      log.debug("Backend actor terminated: {}", ref.actorPath);
      context.unwatch(ref)
    }
  }

}

