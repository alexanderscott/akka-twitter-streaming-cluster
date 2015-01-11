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
import com.github.alexanderscott.twitterstream.ClusterRoles

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
  with ActorLogging with TweetMarshaller with OAuthTwitterAuthorization { this: TwitterAuthorization =>

  import TwitterStreamBackendActor._
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

