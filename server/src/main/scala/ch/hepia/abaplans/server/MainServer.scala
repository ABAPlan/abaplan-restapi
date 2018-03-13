package ch.hepia.abaplans.server

//#quick-start-server
import scala.concurrent.Await
import scala.concurrent.duration.Duration

import akka.actor.{ ActorRef, ActorSystem }
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer

//#main-class
object MainServer extends App with MapRoutes {

  // set up ActorSystem and other dependencies here
  //#main-class
  //#server-bootstrapping
  implicit val system: ActorSystem = ActorSystem("helloAkkaHttpServer")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  //#server-bootstrapping

  val mapRegistryActor: ActorRef = system.actorOf(MapRegistryActor.props, "mapRegistryActor")

  //#main-class
  // from the UserRoutes trait
  lazy val routes: Route = mapRoutes
  //#main-class

  //#http-server
  Http().bindAndHandle(routes, "192.168.1.122", 8080)

  println(s"Server online at http://localhost:8080/")

  Await.result(system.whenTerminated, Duration.Inf)
  //#http-server
  //#main-class
}
//#main-class
//#quick-start-server
