package com.crunchdevelopment.twitterstreaming.core

import akka.actor.{ActorLogging, Props}
import akka.io._
import akka.pattern.ask
import com.crunchdevelopment.twitterstreaming.core.TwitterStreamBackendActor.Protocol.TrackStatusRequest
import com.crunchdevelopment.twitterstreaming.core.TwitterStreamSupervisor.Protocol.GetTracks
import com.typesafe.config.Config
import spray.can.Http
import spray.http.MediaTypes._
import spray.http._
import spray.json._
import spray.routing.HttpServiceActor

object TwitterStreamFrontendActor {
  object Protocol {

  }

  def jsonHttpResponse(response: Option[Any]): HttpResponse = {
    val json = if (response.nonEmpty) response.toJson.toString() else "{}"
    val entity = HttpEntity(ContentTypes.`application/json`, json)
    HttpResponse(entity = entity)
  }

  def failedJsonHttpResponse(e: Throwable): HttpResponse = {
    HttpResponse(status = StatusCodes.InternalServerError, entity = e.toString)
  }

  def props(config: Config): Props = Props(classOf[TwitterStreamFrontendActor], config)
}

class TwitterStreamFrontendActor(config: Config) extends HttpServiceActor with ActorLogging with DefaultJsonProtocol {
  import TwitterStreamFrontendActor._
  import TwitterStreamFrontendActor.Protocol._


  IO(Http)(context.system) ! Http.Bind(self, "0.0.0.0", config.getInt("port"))

  def receive: Receive = runRoute {
    path("api/track") {
      get {
        respondWithMediaType(`application/json`) {
          _.complete {
            (for {
              tracks <- context.parent.ask(GetTracks(Some(self.actorRef))).mapTo[Seq[String]]
            } yield {
              jsonHttpResponse(Some(tracks.toMap))
            }) recover { case e: Throwable =>
              log.error("Error fetching tracks: ", e)
              failedJsonHttpResponse(e)
            }
          }
        }
      } ~
      post {
        entity(as[String]) { request =>
          complete {

            //TODO add track request to backend pool
            context.parent ! TrackStatusRequest(request)

            jsonHttpResponse(None)
          }
        }
      }
    }
  }
   
}


