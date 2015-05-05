package playground.spray

import akka.actor.{ActorSystem, Actor, Props}
import akka.pattern.ask
import akka.util.Timeout

import spray.routing.SimpleRoutingApp

import scala.concurrent.duration._
import scala.util._


object PersonRepository {
  case class Person(name: String, age: Int)

  /* ==== Messages ==== */
  case class SavePerson(name: String, age: Int)
  case class GetPerson(name: String)
  case class DeletePerson(name: String, age: Int)
  case object Done
}


class PersonRepository extends Actor {
  import PersonRepository._

  var persons = List.empty[Person]

  def receive = {
    case SavePerson(name: String, age: Int) => {
      persons :+= Person(name, age)
      sender ! Done
    }

    case GetPerson(name: String) => {
      sender ! persons.filter( _.name == name ).toString
    }

    case DeletePerson(name: String, age: Int) => {
      persons = persons filter { _ != Person(name, age) }
      sender ! Done
    }
  }
}


/**
 * Simple REST API using Spray.
 */
object REST extends App with SimpleRoutingApp {
  import PersonRepository._

  implicit val system = ActorSystem("rest-system")
  implicit val timeout = Timeout(5.seconds)
  implicit val ex = system.dispatcher

  val personRepo = system.actorOf(Props[PersonRepository])

  startServer(interface = "localhost", port = 8998) {
    path("person") {
      put {
        parameter('name, 'age.as[Int]) { (name, age) =>
          onComplete(personRepo ? SavePerson(name, age)) {
            case Success(_) => complete { "Done" }
            case Failure(e) => complete { e }
          }
        }
      } ~
      get {
        parameter('name) { name =>
          onComplete(personRepo ? GetPerson(name)) {
            case Success(p) => complete { p.toString }
            case Failure(e) => complete { e }
          }
        }
      } ~
      delete {
        parameter('name, 'age.as[Int]) { (name, age) =>
          onComplete(personRepo ? DeletePerson(name, age)) {
            case Success(_) => complete { "Done" }
            case Failure(e) => complete { e }
          }
        }
      }
    }
  }

}


// vim: set ts=2 sw=2 et:
