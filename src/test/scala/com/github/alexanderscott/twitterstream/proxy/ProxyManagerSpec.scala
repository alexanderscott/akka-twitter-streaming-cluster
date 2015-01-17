package com.github.alexanderscott.twitterstream.proxy

import com.github.alexanderscott.twitterstream.proxy.ProxyManager.Protocol.GetAvailableProxy
import com.github.alexanderscott.twitterstream.proxy.ProxyManager.TwitterProxy
import com.typesafe.config.ConfigFactory
import org.specs2.matcher._
import akka.testkit._
import akka.pattern.ask
import akka.actor._
import org.scalatest._
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfter, MustMatchers, WordSpecLike}
import org.specs2.mock.Mockito
import scala.concurrent.Await
import scala.concurrent.duration._

object ProxyManagerSpec {
  val testConfig =
    """
       twitterstreaming.backend.proxies = [
          {
            address = "localhost"
            proxyType = "http"
          },
          {
            address = "url2"
            proxyType = "http"
          }
       ]

    """

}

class ProxyManagerSpec
  extends TestKit(ActorSystem("ProxyManagerSpec", ConfigFactory.load(ProxyManagerSpec.testConfig)))
  with ImplicitSender with DefaultTimeout with WordSpecLike with MustMatchers
  with BeforeAndAfter with BeforeAndAfterAll with Mockito {

  var proxyManager = null: TestActorRef[ProxyManager]
  val probe = TestProbe()

  override def beforeAll() = {
    proxyManager = TestActorRef(new ProxyManager)
  }


  override def afterAll() = {
    TestKit.shutdownActorSystem(system)
  }

  "ProxyManager" must {
    "load configured proxies from configuration" in {
      val configuredProxies = proxyManager.underlyingActor.configuredProxies
      assert(configuredProxies.isInstanceOf[Seq[TwitterProxy]])
      assert(configuredProxies.length == 2)
      assert(configuredProxies(0).address == "localhost")
      assert(configuredProxies(0).proxyType == "http")
      assert(configuredProxies(0).address == "url2")
      assert(configuredProxies(0).proxyType == "http")
    }

    "return an available proxy" in {
      probe.send(proxyManager, GetAvailableProxy())
      val res = probe.expectMsgClass(classOf[TwitterProxy])
      assert(proxyManager.underlyingActor.configuredProxies.contains(res))
      assert(proxyManager.underlyingActor.proxyConsumerMap(res) == probe.ref)
      assert(proxyManager.underlyingActor.proxyUsageMap(res) == 1)
    }
  }

}
