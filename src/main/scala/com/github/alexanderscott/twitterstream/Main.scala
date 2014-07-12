package com.github.alexanderscott.twitterstream

import akka.actor.{ActorSystem, Props}
import com.github.alexanderscott.twitterstream.core.{OAuthTwitterAuthorization, TweetStreamerActor}

import scala.annotation.tailrec

object Main extends App {
  import com.github.alexanderscott.twitterstream.Commands._

  val system = ActorSystem()
  val stream = system.actorOf(Props(new TweetStreamerActor(TweetStreamerActor.twitterUri) with OAuthTwitterAuthorization))

  @tailrec
  private def commandLoop(): Unit = {
    Console.readLine() match {
      case QuitCommand         => return
      case TrackCommand(query) => stream ! query
      case _                   => println("unknown")
    }

    commandLoop()
  }

  // start processing the commands
  commandLoop()

}

object Commands {

  val QuitCommand   = "quit"
  val TrackCommand = "track (.*)".r

}
