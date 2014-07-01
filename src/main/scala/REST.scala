package com.ataraxer.sprayer

import akka.actor.ActorSystem
import spray.routing.SimpleRoutingApp


case class Person(name: String, age: Int)


/**
 * Simple REST API using Spray.
 */
object REST extends App with SimpleRoutingApp {
  implicit val system = ActorSystem("rest-system")

  var persons = List.empty[Person]

  startServer(interface = "localhost", port = 8998) {
    path("person") {
      put {
        parameter('name, 'age.as[Int]) { (name, age) =>
          persons :+= Person(name, age)
          complete { "Done!" }
        }
      } ~
      get {
        parameter('name) { name =>
          complete {
            persons.filter( _.name == name ).toString
          }
        }
      } ~
      delete {
        parameter('name, 'age.as[Int]) { (name, age) =>
          persons = persons filter { _ != Person(name, age) }
          complete { "Done!" }
        }
      }
    }
  }

}


// vim: set ts=2 sw=2 et:
