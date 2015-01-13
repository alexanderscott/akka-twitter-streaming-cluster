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

object TwitterStreamProxyManager {

  case class Proxy(address: String, proxyType: String)

  object Protocol {
    trait Request[T]
    case object GetAvailableProxy extends Request[Proxy]
    case object GetConfiguredProxies extends Request[Seq[Proxy]]

    case class UseProxy(proxy: Proxy, address: Address)
    case class UnuseProxy(proxy: Proxy, address: Address)

    case object ProxyLimitExceeded extends Throwable
  }

  val proxyUsageLimit = 5000

  def props(config: Config): Props = Props(classOf[TwitterStreamProxyManager], config)
}

class TwitterStreamProxyManager(config: Config) extends Actor with ActorLogging {
  import TwitterStreamProxyManager._
  import TwitterStreamProxyManager.Protocol._

  val configuredProxies: Seq[Proxy] = config.getList("proxies").unwrapped().toArray.map(_.asInstanceOf[Proxy]).toSeq

  val proxyAddressMap = concurrent.TrieMap.empty[Proxy, Address]
  val proxyUsageMap = concurrent.TrieMap.empty[Proxy, Int].withDefaultValue(0)

  def receive: Receive = {
    case GetAvailableProxy => {
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

    case UseProxy(proxy: Proxy, address: Address) => {
      if (proxyAddressMap.contains(proxy) && proxyAddressMap(proxy) != address) {
        log.error("Address {} already using proxy {}", proxyAddressMap(proxy).host, proxy.address)
        throw new Exception("Proxy in-use")
      } else {
        proxyAddressMap += proxy -> address
        proxyUsageMap(proxy) += 1
      }
    }

    case UnuseProxy(proxy: Proxy, address: Address) => {
      if (proxyAddressMap.contains(proxy) && proxyAddressMap(proxy) != address) {
        proxyUsageMap(proxy) -= 1
      } else {
        log.warning("Tried to unuse proxy {} which is not in use", proxy.address)
      }

      if (proxyUsageMap(proxy) == 0) proxyAddressMap.remove(proxy)

    }

    case x: Any => log.warning("Received unknown message: {}", x.toString)
  }

}
