package com.github.alexanderscott.twitterstream.core

import com.typesafe.config.ConfigFactory
import org.specs2.matcher._
import akka.testkit._
import akka.pattern.ask
import akka.actor._
import org.scalatest._
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfter, MustMatchers, WordSpecLike}
import org.specs2.mock.Mockito

object TwitterStreamBackendActorSpec {
  val testConfig =
    """
     twitterstreaming.backend {
        streamUrl = "https://stream.twitter.com/1.1/statuses/filter.json"
        proxies = [
          {
            address = "localhost"
            proxyType = "http"
          },
          {
            address = "url2"
            proxyType = "http"
          }
        ]
     }
    """
}

class TwitterStreamBackendActorSpec
  extends TestKit(ActorSystem("TwitterStreamBackendActorSpec", ConfigFactory.load(TwitterStreamBackendActorSpec.testConfig)))
  with ImplicitSender with DefaultTimeout with WordSpecLike with MustMatchers
  with BeforeAndAfter with BeforeAndAfterAll with Mockito {

  var streamBackend = null: TestActorRef[TwitterStreamBackendActor]
  val probe = TestProbe()

  override def beforeAll() = {
    streamBackend = TestActorRef(new TwitterStreamBackendActor(TwitterStreamBackendActor.twitterUri, probe.ref))
  }


  override def afterAll() = {
    TestKit.shutdownActorSystem(system)
  }

  "ProxyManager" must {

  }

}

