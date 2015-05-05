package playground.spray

import akka.io.IO
import akka.actor.{ActorSystem, Actor, Props}
import akka.actor.Actor.Receive

import spray.can.Http
import spray.http._
import spray.http.HttpMethods._
import spray.routing._


object Server extends App {
  implicit val system = ActorSystem("sprayer-system")

  val server = system.actorOf(Props[Server], name = "sparyer-server")

  IO(Http) ! Http.Bind(server, interface = "localhost", port = 8080)
}


class Server extends HttpServiceActor {
  val echoRoute = get {
    path("echo") {
      parameters('msg) { msg =>
        complete { "Echo: " + msg }
      }
    }
  }


  val manualHandlers: Receive = {
    case Http.Connected(remoteAddress, localAddress) => {
      println("Connection established!")
      println("Remote address: " + remoteAddress)
      println("Local address: " + localAddress)
      sender ! Http.Register(self)
    }

    case HttpRequest(GET, Uri.Path("/ping"), _, _, _) => {
      sender ! HttpResponse(entity = "Pong!")
    }
  }


  def receive = manualHandlers orElse runRoute(echoRoute)
}


// vim: set ts=2 sw=2 et:
