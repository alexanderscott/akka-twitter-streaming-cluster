package com.github.alexanderscott.twitterstream.core

import akka.actor.{ActorLogging, Props}
import akka.io._
import com.github.alexanderscott.twitterstream.core.TwitterStreamBackendActor.Protocol.TrackStatusRequest
import com.github.alexanderscott.twitterstream.core.TwitterStreamSupervisor.Protocol.GetTracks
import com.typesafe.config.Config
import spray.can.Http
import spray.http._
import spray.routing.HttpServiceActor
import akka.pattern.ask
import akka.pattern.pipe
import spray.json._

object TwitterStreamFrontendActor {
  object Protocol {

  }

  def props(config: Config): Props = Props(classOf[TwitterStreamFrontendActor], config)
}

class TwitterStreamFrontendActor(config: Config) extends HttpServiceActor with ActorLogging {


  IO(Http)(context.system) ! Http.Bind(self, "0.0.0.0", config.getInt("port"))

  def receive: Receive = runRoute {
    path("api/track") {
      get {
        complete {
          (for {
            tracks <- context.parent.ask(GetTracks(Some(self.actorRef))).mapTo[Seq[String]]
          } yield {
            val entity = HttpEntity(ContentTypes.`application/json`, tracks.toMap.toJson.toString)
            HttpResponse(entity = entity)
          }) recover { case e: Throwable =>
            log.error("Error fetching tracks: ", e)
            HttpResponse(status = StatusCodes.InternalServerError)
          }
        }
      } ~
      post {
        entity(as[String]) { request =>
          complete {

            //TODO add track request to backend pool
            context.parent ! TrackStatusRequest(request)

            val entity = HttpEntity(ContentTypes.`application/json`, "{}")
            HttpResponse(entity = entity)

          }
        }
      }
    }
  }
   
}


