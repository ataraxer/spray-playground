package playground.spray

import akka.actor.{Actor, ActorContext, ActorSystem, Props}
import akka.io.IO
import akka.pattern.ask

import spray.can._
import spray.http.{HttpRequest, HttpResponse, Uri}
import spray.http.HttpMethods.GET
import spray.routing.{Route, Directive1, HttpService}

import shapeless.{HNil, ::}

import scala.util.{Success, Failure}
import scala.concurrent.duration._


class DemoServer extends Actor with HttpService with BlackboxSupport {
  // required by `HttpSerivce` trait
  def actorRefFactory = context

  def receive = runRoute {
    get {
      path("auth") {
        complete("ataraxer")
      } ~
      path("ping") {
        blackbox(5.seconds, context) { user =>
          complete(user)
        }
      }
    }
  }
}


trait BlackboxSupport {
  def blackbox(
    timeout: FiniteDuration,
    context: ActorContext): Directive1[String] =
  {
    new Directive1[String] {
      def happly(f: (String :: HNil) => Route): Route = { ctx =>
        import context.system
        import context.dispatcher

        val request = HttpRequest(GET, Uri("http://localhost:8080/auth"), Nil)
        val auth = (IO(Http) ? request)(timeout)

        auth onComplete {
          case Success(response: HttpResponse) => {
            val user = response.entity.asString
            f(user :: HNil)(ctx)
          }

          case Failure(reason) => ctx.failWith(reason)
        }
      }
    }
  }
}


object CustomSprayDirective extends App {
  implicit val system = ActorSystem("custom-spray-directive")
  val server = system actorOf Props[DemoServer]
  IO(Http) ! Http.Bind(server, "localhost", 8080)
}

