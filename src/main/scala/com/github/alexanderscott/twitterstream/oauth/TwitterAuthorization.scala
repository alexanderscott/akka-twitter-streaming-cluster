package com.github.alexanderscott.twitterstream.oauth

import com.typesafe.config.ConfigFactory
import spray.http.HttpRequest
import scala.io.Source
import com.github.alexanderscott.twitterstream.oauth.OAuth._

trait TwitterAuthorization {
  def authorize: HttpRequest => HttpRequest
}

trait OAuthTwitterAuthorization extends TwitterAuthorization {

  /*
  val home = System.getProperty("user.home")
  val lines = Source.fromFile(s"$home/.twitter/activator").getLines().toList

  val consumer = Consumer(lines(0), lines(1))
  val token = Token(lines(2), lines(3))
  */

  val twitterAuthConfig = ConfigFactory.load().atKey("twitterstreaming.backend.auth")
  private[this] val consumerKey = twitterAuthConfig.getString("consumer-key")
  private[this] val consumerSecret = twitterAuthConfig.getString("consumer-secret")
  private[this] val accessToken = twitterAuthConfig.getString("access-token")
  private[this] val accessTokenSecret = twitterAuthConfig.getString("access-token-secret")

  val consumer = Consumer(consumerKey, consumerSecret)
  val token = Token(accessToken, accessTokenSecret)

  val authorize: (HttpRequest) => HttpRequest = oAuthAuthorizer(consumer, token)
}
