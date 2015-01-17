package com.github.alexanderscott.twitterstream.auth

import com.typesafe.config.ConfigFactory
import spray.http.HttpRequest
import com.github.alexanderscott.twitterstream.auth.OAuth._

trait TwitterAuthorization {
  def authorize: HttpRequest => HttpRequest
}

trait OAuthTwitterAuthorization extends TwitterAuthorization {

  val twitterAuthConfig = ConfigFactory.load().getConfig("twitterstreaming.backend.auth")
  private[this] val consumerKey = twitterAuthConfig.getString("consumer-key")
  private[this] val consumerSecret = twitterAuthConfig.getString("consumer-secret")
  private[this] val accessToken = twitterAuthConfig.getString("access-token")
  private[this] val accessTokenSecret = twitterAuthConfig.getString("access-token-secret")

  val consumer = Consumer(consumerKey, consumerSecret)
  val token = Token(accessToken, accessTokenSecret)

  val authorize: (HttpRequest) => HttpRequest = oAuthAuthorizer(consumer, token)
}
