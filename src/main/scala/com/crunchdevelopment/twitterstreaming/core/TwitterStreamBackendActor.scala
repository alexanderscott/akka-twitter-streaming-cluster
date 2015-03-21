package com.crunchdevelopment.twitterstreaming.core

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.io.IO
import com.crunchdevelopment.twitterstreaming.auth._
import spray.can.Http
import spray.client.pipelining._
import spray.http.{HttpRequest, _}

object TwitterStreamBackendActor {
  val twitterUri = Uri("https://stream.twitter.com/1.1/statuses/filter.json")

  object Protocol {

    case class TrackStatusRequest(query: String)
    case class TrackStatusResponse(success: Boolean)
  }

  def props(handler: ActorRef): Props = {
    Props(classOf[TwitterStreamBackendActor], twitterUri, handler)
  }
}

class TwitterStreamBackendActor(uri: Uri, handler: ActorRef) extends Actor
  with ActorLogging with TweetMarshaller with OAuthTwitterAuthorization {
  import TwitterStreamBackendActor.Protocol._

  val io = IO(Http)(context.system)

  def receive: Receive = {
    case TrackStatusRequest(query: String) =>
      val body = HttpEntity(ContentType(MediaTypes.`application/x-www-form-urlencoded`), s"track=$query")
      val rq = HttpRequest(HttpMethods.POST, uri = uri, entity = body) ~> authorize
      sendTo(io).withResponsesReceivedBy(self)(rq)

    case ChunkedResponseStart(_) =>

    case MessageChunk(entity, _) => TweetUnmarshaller(entity).fold(_ => (), handler !)

    case _ =>
  }
}

