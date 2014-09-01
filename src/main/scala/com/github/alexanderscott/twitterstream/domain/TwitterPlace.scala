package com.github.alexanderscott.twitterstream.domain

case class TwitterPlace(country: String, name: String) {
  override lazy val toString = s"$name, $country"
}

