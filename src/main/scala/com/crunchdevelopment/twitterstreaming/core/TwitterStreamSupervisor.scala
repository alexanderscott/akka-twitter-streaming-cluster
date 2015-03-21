package com.crunchdevelopment.twitterstreaming.core

import akka.actor.{ActorRef, Actor, Props}
import scala.concurrent.Future
import spray.http._
import akka.actor._
import com.crunchdevelopment.twitterstreaming.auth._
import com.crunchdevelopment.twitterstreaming.ClusterRoles
import akka.cluster.Cluster
import com.typesafe.config.Config
import akka.pattern.ask
import akka.pattern.pipe
import scala.collection.concurrent
import scala.collection.mutable


object TwitterStreamSupervisor {
  val twitterUri = Uri("https://stream.twitter.com/1.1/statuses/filter.json")

  object Protocol {
    case class GetTracks(user: Option[ActorRef])

  }

  def props(config: Config): Props = Props(classOf[TwitterStreamSupervisor], config)
}

class TwitterStreamSupervisor(config: Config) extends Actor with ActorLogging {
  import TwitterStreamSupervisor._
  import TwitterStreamSupervisor.Protocol._
  import TwitterStreamBackendActor.Protocol._
  import TwitterStreamFrontendActor.Protocol._
  import context.system

  var streamBackend = system.deadLetters
  var streamFrontend = system.deadLetters

  var tracked = concurrent.TrieMap.empty[ActorRef, mutable.Seq[String]].withDefaultValue(mutable.Seq.empty[String])

  val webservicePort = config.getString("web.port")

  override def preStart(): Future[Unit] = {
    if (Cluster(system).getSelfRoles.contains(ClusterRoles.Backend)){
      streamBackend = context.actorOf(TwitterStreamBackendActor.props(self))
      context.watch(streamBackend)
    }

    if (Cluster(system).getSelfRoles.contains(ClusterRoles.Frontend)){
      streamFrontend = context.actorOf(TwitterStreamBackendActor.props(self))
      context.watch(streamFrontend)
    }

    Future.successful(Unit)
  }

  def receive: Receive = {

    case x: TrackStatusRequest => {
      tracked(sender().actorRef) += x.query
      streamBackend ? x pipeTo sender()
    }

    case GetTracks(user: Option[ActorRef]) => {
      if(user.nonEmpty) {
        sender() ! tracked(user.get)
      } else {
        sender() ! tracked.values.flatten
      }
    }

    case Terminated(ref: ActorRef) => {
      tracked.remove(ref)
      log.debug("Actor terminated: {}", ref.path);
      context.unwatch(ref)
    }

  }

}

