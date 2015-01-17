package com.github.alexanderscott.twitterstream.proxy

import akka.actor._
import akka.util.Timeout
import scala.concurrent.Future
import scala.concurrent.duration._
import akka.contrib.pattern.DistributedPubSubExtension
import akka.contrib.pattern.DistributedPubSubMediator
import akka.contrib.pattern.DistributedPubSubMediator.Put
import scala.concurrent.duration.Deadline
import scala.concurrent.duration.FiniteDuration
import akka.actor.Props
import akka.contrib.pattern.ClusterReceptionistExtension
import akka.cluster.Cluster
import akka.persistence.PersistentActor
import com.typesafe.config._
import scala.collection.concurrent

object ProxyManager {

  case class TwitterProxy(address: String, proxyType: String, proxy: Proxy)

  object Protocol {
    sealed trait Request[T]
    case class GetAvailableProxy() extends Request[TwitterProxy]
    case class GetConfiguredProxies() extends Request[Seq[TwitterProxy]]

    case class UseProxy(proxy: TwitterProxy, address: Address) extends Request[TwitterProxy]
    case class UnuseProxy(proxy: TwitterProxy, address: Address) extends Request[TwitterProxy]

    case object InvalidProxy extends Throwable
    case object ProxyLimitExceeded extends Throwable
  }

  val proxyUsageLimit = 5000

  def props: Props = Props(classOf[ProxyManager])
}

class ProxyManager extends Actor with ActorLogging {
  import ProxyManager._
  import ProxyManager.Protocol._

  val configuredProxies: Seq[TwitterProxy] =
    ConfigFactory.load().getConfig("twitterstreaming").getList("proxies").unwrapped().toArray.map(
      _.asInstanceOf[TwitterProxy]
    ).toSeq

  val proxyConsumerMap = concurrent.TrieMap.empty[TwitterProxy, ActorRef]
  val proxyUsageMap = concurrent.TrieMap.empty[TwitterProxy, Int].withDefaultValue(0)

  def useProxy(proxy: TwitterProxy, consumer: ActorRef): Proxy = {
    if (proxyConsumerMap.contains(proxy) && proxyConsumerMap(proxy) != consumer) {
      log.error("Actor {} already using proxy {}", proxyConsumerMap(proxy).path, proxy.address)
      throw new Exception("Proxy in-use")
    } else {
      proxyConsumerMap += proxy -> consumer
      proxyUsageMap(proxy) += 1
    }
    proxy
  }

  def unuseProxy(proxy: TwitterProxy, consumer: ActorRef): Proxy = {
    if (proxyConsumerMap.contains(proxy) && proxyConsumerMap(proxy) != consumer) {
      proxyUsageMap(proxy) -= 1
    } else {
      log.warning("Tried to unuse proxy {} which is not in use", proxy.address)
    }

    if (proxyUsageMap(proxy) == 0) proxyConsumerMap.remove(proxy)
    proxy
  }

  def receive: Receive = {
    case Terminated(ref: ActorRef) => {
      context.unwatch(ref)
      if (proxyConsumerMap.values.toSeq.contains(ref)) {
        val usedProxy = proxyConsumerMap.filter(_._2 == ref).keys.toSeq(0)
        unuseProxy(usedProxy, ref)
      }
    }

    case GetAvailableProxy() => {
      val availableProxy = proxyUsageMap.find(proxyUsage => proxyUsage._2 < proxyUsageLimit).map(_._1)
      if (availableProxy.nonEmpty) {
        log.debug("Found available proxy: {}", availableProxy.get.address)
        sender() ! availableProxy
      } else {
        val newProxy = configuredProxies.filterNot(proxy => proxyUsageMap.map(_._1).toSeq.contains(proxy)).headOption
        if (newProxy.nonEmpty) {
          log.warning("Proxy usage at limit, activating new proxy at {} ", newProxy.get.address)
          proxyUsageMap(newProxy.get) = 0
          sender() ! newProxy.get
        } else {
          log.error("Proxy usage exceeded")
          sender() ! ProxyLimitExceeded
        }
      }
    }

    case GetConfiguredProxies() => {
      sender() ! proxyConsumerMap.keys.toSeq
    }

    case UseProxy(proxy: TwitterProxy, address: Address) => {
      context.watch(sender())
      sender() ! useProxy(proxy, sender())
    }

    case UnuseProxy(proxy: TwitterProxy, address: Address) => {
      context.unwatch(sender())
      sender() ! unuseProxy(proxy, sender())
    }

    case x: Any => log.warning("Received unknown message: {}", x.toString)
  }

}
