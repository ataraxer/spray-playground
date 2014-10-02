package com.ataraxer.sprayer

import akka.io.IO
import akka.actor._
import akka.actor.Actor.Receive

import spray.can.Http
import spray.http._
import spray.http.HttpMethods._
import spray.http.StatusCodes._
import spray.routing._


/**
 * ConnectionDispatcher controls underlying TCP-connections flow
 * of HTTP requests.
 */
object ConnectionDispatcher extends App {
  implicit val system = ActorSystem("dispatcher-system")

  val dispatcher = system actorOf (
    Props[ConnectionDispatcher],
    name = "connection-dispatcher")

  IO(Http) ! Http.Bind(dispatcher, interface = "localhost", port = 51115)
}


class ConnectionDispatcher extends Actor {
  def receive = {
    case Http.Connected(remoteAddress, localAddress) => {
      val handler = context actorOf {
        Props(classOf[RequestHandler], sender)
      }

      sender ! Http.Register(handler)
    }
  }
}


class RequestHandler(connection: ActorRef) extends HttpServiceActor {
  private var requestedPing = false

  val route = get {
    path("ping") { ctx =>
      requestedPing = true
      ctx.complete("Pong!")
    } ~
    pathPrefix("api") { ctx =>
      if (requestedPing) {
        val echoer = context.actorOf(Props[Echoer])
        echoer ! ctx
      } else {
        ctx.complete(BadRequest -> "Ping me first!")
      }
    }
  }

  def receive = runRoute(route)
}


class Echoer extends HttpServiceActor {
  val process = path("echo") {
    parameters('msg) { msg =>
      complete(msg)
    }
  }

  def receive = {
    case ctx: RequestContext => {
      process(ctx)
    }
  }
}


// vim: set ts=2 sw=2 et:
