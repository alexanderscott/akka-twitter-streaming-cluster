package com.github.alexanderscott.twitterstream.core

import akka.actor.{ActorRef, Actor, Props, ActorLogging}
import spray.http._
import spray.can.Http
import spray.http.HttpRequest
import akka.io._
import spray.routing._
import spray.io._
import spray.httpx._

object TweetStreamFrontendActor {
  def props(): Props = Props(classOf[TweetStreamFrontendActor])
}

class TweetStreamFrontendActor extends Actor with ActorLogging {

  IO(Http)(context.system) ! Http.Bind(self, "0.0.0.0", 9090)

  def receive: Receive = runRoute(
    path("api/track") { 
      post {
        complete {
          val entity = HttpEntity(ContentTypes.`application/json`, ))
          HttpResponse(entity = entity)
        }
      }
    }
  }
   
}


