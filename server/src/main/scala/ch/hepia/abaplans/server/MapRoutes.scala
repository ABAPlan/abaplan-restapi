package ch.hepia.abaplans.server

import akka.actor.{ ActorRef, ActorSystem }
import akka.event.Logging
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MethodDirectives.{ delete, get, post }
import akka.http.scaladsl.server.directives.PathDirectives.path
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.pattern.ask
import akka.util.Timeout
import ch.hepia.abaplans.server.MapRegistryActor._

import scala.concurrent.Future
import scala.concurrent.duration._

//#user-routes-class
trait MapRoutes extends JsonSupport {
  //#user-routes-class

  // we leave these abstract, since they will be provided by the App
  implicit def system: ActorSystem

  lazy val log = Logging(system, classOf[MapRoutes])

  // other dependencies that UserRoutes use
  def mapRegistryActor: ActorRef

  // Required by the `ask` (?) method below
  implicit lazy val timeout = Timeout(20 seconds) // usually we'd obtain the timeout from the system's configuration

  //#all-routes
  //#users-get-post
  //#users-get-delete   
  lazy val mapRoutes: Route =
    pathPrefix("users") {
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
                val mapCreated: Future[ActionPerformed] =
                  (mapRegistryActor ? CreateMap(map)).mapTo[ActionPerformed]
                onSuccess(mapCreated) { performed =>
                  log.info("Created map [{}]: {}", map.id, performed.description)
                  complete((StatusCodes.Created, performed))
                }
              }
            }
          )
        },
        //#users-get-post
        //#users-get-delete
        path(Segment) { id =>
          concat(
            get {
              //#retrieve-user-info
              val maybeMap: Future[Option[ArcgisMap]] =
                (mapRegistryActor ? GetMap(id)).mapTo[Option[ArcgisMap]]
              rejectEmptyResponse {
                complete(maybeMap)
              }
              //#retrieve-user-info
            },
            delete {
              //#users-delete-logic
              val userDeleted: Future[ActionPerformed] =
                (mapRegistryActor ? DeleteMap(id)).mapTo[ActionPerformed]
              onSuccess(userDeleted) { performed =>
                log.info("Deleted map [{}]: {}", id, performed.description)
                complete((StatusCodes.OK, performed))
              }
              //#users-delete-logic
            }
          )
        }
      )
      //#users-get-delete
    }
  //#all-routes
}