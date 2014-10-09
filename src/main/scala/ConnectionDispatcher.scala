package com.ataraxer.sprayer

import akka.io.IO
import akka.actor._
import akka.actor.Actor.Receive
import akka.event.Logging
import akka.event.Logging._

import spray.can.Http
import spray.http._
import spray.http.HttpMethods._
import spray.http.StatusCodes._
import spray.routing._
import spray.routing.directives._


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


object RequestHandler {
  def loggerImpl(request: HttpRequest) = {
    val requestTime = System.currentTimeMillis

    val responseProcessor: PartialFunction[Any, Unit] = {
      case response: HttpResponse =>
        val responseTime = System.currentTimeMillis - requestTime
        println(
          request.method + " " +
          request.uri.toRelative + " " +
          response.status + " " +
          responseTime)
      case other =>
        println(request.uri + " " + other)
    }

    responseProcessor.apply _
  }

  val logger = LoggingMagnet(loggerImpl _)
}


class RequestHandler(connection: ActorRef) extends HttpServiceActor {
  import RequestHandler._

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

  def receive = runRoute {
    logRequestResponse(logger)(route)
  }
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
