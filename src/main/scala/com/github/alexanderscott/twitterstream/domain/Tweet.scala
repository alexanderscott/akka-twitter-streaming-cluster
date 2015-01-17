package com.github.alexanderscott.twitterstream.domain

case class Tweet(id: String, user: TwitterUser, text: String, place: Option[TwitterPlace])

case class TwitterUser(id: String, lang: String, followersCount: Int)

case class TwitterPlace(country: String, name: String) {
  override lazy val toString = s"$name, $country"
}

