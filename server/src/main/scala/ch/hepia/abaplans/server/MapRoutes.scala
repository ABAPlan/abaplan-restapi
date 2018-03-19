package ch.hepia.abaplans.server

import akka.actor.{ ActorRef }
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MethodDirectives.{ delete, get, post }
import akka.http.scaladsl.server.directives.PathDirectives.path
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.pattern.ask
import akka.util.Timeout
import ch.hepia.abaplans.server.MapRegistryActor._

import akka.http.scaladsl.model.StatusCodes

import scala.concurrent.Future
import scala.concurrent.duration._

//#user-routes-class
trait MapRoutes extends JsonSupport {
  // we leave these abstract, since they will be provided by the App implicit def system: ActorSystem
  def mapRegistryActor: ActorRef

  // Required by the `ask` (?) method below
  implicit lazy val timeout = Timeout(20 seconds) // usually we'd obtain the timeout from the system's configuration

  import ch.megard.akka.http.cors.scaladsl.CorsDirectives._

  //#all-routes
  lazy val mapRoutes: Route = cors() {
    pathPrefix("maps") {
      concat(
        //#users-get-delete
        pathEnd {
          concat(
            get {
              val maps: Future[ArcgisMaps] = (mapRegistryActor ? GetMaps).mapTo[ArcgisMaps]
              complete(maps)
            },
            post {
              entity(as[ArcgisMap]) { map =>
                println("test")
                println(map)
                val mapCreated: Future[MapCreated] =
                  (mapRegistryActor ? CreateMap(map)).mapTo[MapCreated]
                onSuccess(mapCreated) { map =>
                  complete((StatusCodes.Created, map))
                }
              }
            }
          )
        },
        path(Segment) { id =>
          concat(
            get {
              //#retrieve-user-info
              val maybeMap: Future[Option[ArcgisMap]] =
                (mapRegistryActor ? GetMap(id)).mapTo[Option[ArcgisMap]]
              rejectEmptyResponse {
                complete(maybeMap)
              }
            },
            delete {
              val userDeleted: Future[ActionPerformed] =
                (mapRegistryActor ? DeleteMap(id)).mapTo[ActionPerformed]
              onSuccess(userDeleted) { performed =>
                complete((StatusCodes.OK, performed))
              }
            }
          )
        }
      )
    }
  }
  //#all-routes
}
