package com.ataraxer.sprayer

import akka.io.IO
import akka.actor.{ActorSystem, Actor, Props}

import spray.can.Http
import spray.http._
import spray.http.HttpMethods._


/**
 *
 */
object Server extends App {
  implicit val system = ActorSystem("sprayer-system")

  val server = system.actorOf(Props[Server], name = "sparyer-server")

  IO(Http) ! Http.Bind(server, interface = "localhost", port = 8080)
}


class Server extends Actor {
  def receive = {
    case Http.Connected(remoteAddress, localAddress) => {
      println("Connection established!")
      println("Remote address: " + remoteAddress)
      println("Local address: " + localAddress)
      sender ! Http.Register(self)
    }

    case HttpRequest(GET, Uri.Path("/ping"), _, _, _) => {
      sender ! HttpResponse(entity = "Pong!")
    }

    case request: HttpRequest => {
      sender ! HttpResponse(entity = "Unknown request: %s".format(request))
    }
  }
}


// vim: set ts=2 sw=2 et:
