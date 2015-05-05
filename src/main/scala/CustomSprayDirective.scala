package playground.spray

import akka.actor._
import akka.io._
import akka.pattern.ask

import spray.can._
import spray.http._
import spray.routing._

import shapeless._

import scala.util.{Success, Failure}
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.implicitConversions


class Server extends Actor with HttpService with BlackboxSupport {
  def actorRefFactory = context

  def receive = runRoute {
    get {
      path("auth") {
        complete("ataraxer")
      } ~
      path("ping") {
        blackbox(5.seconds) { user =>
          complete(user)
        }
      }
    }
  }
}


trait BlackboxSupport {
  def blackbox(magnet: BlackboxMagnet): Directive1[String] = magnet
}


trait BlackboxMagnet extends Directive1[String]


object BlackboxMagnet extends directives.HeaderDirectives {
  private var authorizedUsers = Set.empty[String]


  implicit def fromContext
    (timeout: FiniteDuration)
    (implicit context: ActorContext): BlackboxMagnet =
  {
    new BlackboxMagnet {
      def happly(f: (String :: HNil) => Route) = {
        optionalHeaderValueByName("Authorization") {
          case Some(user) => ctx => {
            if (authorizedUsers contains user) {
              ctx.complete(f"Already authorized: $user")
            } else {
              ctx.reject(AuthorizationFailedRejection)
            }
          }


          case None => ctx => {
            import context.system
            import context.dispatcher

            val request = HttpRequest(HttpMethods.GET, Uri("http://localhost:8080/auth"), Nil)
            val auth = (IO(Http) ? request)(timeout)

            auth onComplete {
              case Success(response: HttpResponse) => {
                val user = response.entity.asString
                authorizedUsers += user
                f(user :: HNil)(ctx)
              }

              case Failure(reason) => ctx.failWith(reason)
            }
          }
        }
      }
    }
  }
}


object CustomSprayDirective extends App {
  implicit val system = ActorSystem("custom-spray-directive")
  val server = system actorOf Props[Server]
  IO(Http) ! Http.Bind(server, "localhost", 8080)
}

