package com.github.alexanderscott.twitterstream.core

import akka.actor.{ActorRef, Actor, Props, ActorLogging}
import spray.http._
import spray.httpx._
import spray.httpx.TransformerPipelineSupport
import spray.can.Http
import spray.http.HttpRequest
import spray.io._
import akka.io._
import spray.routing._
import com.github.alexanderscott.twitterstream.oauth._

object TweetStreamBackendActor {
  val twitterUri = Uri("https://stream.twitter.com/1.1/statuses/filter.json")

  def props(): Props = Props(classOf[TweetStreamBackendActor], twitterUri)
}

class TweetStreamBackendActor(uri: Uri, processor: ActorRef) extends Actor with ActorLogging with TweetMarshaller {
  this: TwitterAuthorization =>
  val io = IO(Http)(context.system)

  def receive: Receive = {
    case query: String =>
      val body = HttpEntity(ContentType(MediaTypes.`application/x-www-form-urlencoded`), s"track=$query")
      val rq = HttpRequest(HttpMethods.POST, uri = uri, entity = body) ~> authorize
      sendTo(io).withResponsesReceivedBy(self)(rq)
    case ChunkedResponseStart(_) =>
    case MessageChunk(entity, _) => TweetUnmarshaller(entity).fold(_ => (), processor !)
    case _ =>
  }
}

