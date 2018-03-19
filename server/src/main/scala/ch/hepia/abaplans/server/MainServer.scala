package ch.hepia.abaplans.server

import scala.concurrent.Await
import scala.concurrent.duration.Duration

import akka.actor.{ ActorRef, ActorSystem }
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer

object MainServer extends App with MapRoutes {

  implicit val system: ActorSystem = ActorSystem("helloAkkaHttpServer")
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  val mapRegistryActor: ActorRef = system.actorOf(MapRegistryActor.props, "mapRegistryActor")

  lazy val routes: Route = mapRoutes

  Http().bindAndHandle(routes, "localhost", 8080)

  println("Server online")

  Await.result(system.whenTerminated, Duration.Inf)
}
