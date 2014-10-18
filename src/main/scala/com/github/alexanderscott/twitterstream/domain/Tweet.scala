package com.github.alexanderscott.twitterstream.domain

case class Tweet(id: String, user: TwitterUser, text: String, place: Option[TwitterPlace])
