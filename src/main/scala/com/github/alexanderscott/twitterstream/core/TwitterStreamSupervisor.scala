package com.github.alexanderscott.twitterstream.core

import akka.actor.{ActorRef, Actor, Props}
import spray.http._
import akka.actor._
import com.github.alexanderscott.twitterstream.oauth._


object TweetStreamSupervisor {
  val twitterUri = Uri("https://stream.twitter.com/1.1/statuses/filter.json")
  def props(): Props = Props(classOf[TweetStreamSupervisor], twitterUri)
}

class TweetStreamSupervisor extends Actor with ActorLogging {
  import TweetStreamBackendActor._
  //val streamBackend = context.system.actorOf(Props(new TweetStreamBackendActor(twitterUri, self) with OAuthTwitterAuthorization), ClusterRoles.Backend)
  //context.watch(streamBackend)

  def receive: Receive = {
    case Terminated(ref: ActorRef) => {
      log.debug("Backend actor terminated: {}", ref.path);
      context.unwatch(ref)
    }
  }

}

