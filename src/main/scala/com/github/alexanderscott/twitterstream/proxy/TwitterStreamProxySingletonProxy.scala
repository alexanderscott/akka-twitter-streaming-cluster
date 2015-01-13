package com.github.alexanderscott.twitterstream.proxy

import akka.actor.Actor
import akka.cluster.Cluster
import akka.cluster.ClusterEvent.{MemberEvent, CurrentClusterState, MemberRemoved, MemberUp}
import akka.cluster.Member
import scala.collection.immutable
import scala.concurrent.Future
import akka.contrib.pattern.{ClusterSingletonProxy, DistributedPubSubExtension, DistributedPubSubMediator, ClusterReceptionistExtension}
import akka.contrib.pattern.DistributedPubSubMediator.Put
import scala.concurrent.duration.Deadline
import scala.concurrent.duration.FiniteDuration
import akka.actor.Props
import akka.cluster.Cluster
import akka.persistence.PersistentActor
import com.typesafe.config.Config
import scala.concurrent.duration._

object TwitterStreamProxySingletonProxy {

  def props(config: Config, singletonPath: String, role: Option[String]): Props = {
    Props(classOf[TwitterStreamProxySingletonProxy], config, singletonPath, role)
  }
}

class TwitterStreamProxySingletonProxy(config: Config, singletonPath: String, role: Option[String])
  extends ClusterSingletonProxy(singletonPath, role, 1.second) {

  private val ageOrdering = Ordering.fromLessThan[Member] {
    (a, b) => a.isOlderThan(b)
  }

  private var membersByAge: immutable.SortedSet[Member] = immutable.SortedSet.empty(ageOrdering)

  override def postStop(): Unit = {
    Cluster(context.system).unsubscribe(self)
  }

  override def preStart(): Future[Unit] = {
    Cluster(context.system).subscribe(self, classOf[MemberEvent])
    Future.successful()
  }

  /*
  def receive: Receive = {
    case state: CurrentClusterState => {
      membersByAge = immutable.SortedSet.empty(ageOrdering) ++ state.members
    }

    case MemberUp(m) => {
      membersByAge += m
    }

    case MemberRemoved(m, _) => {
      membersByAge -= m
    }
  }
  */
}

