package ch.hepia.abaplans.server

//#user-registry-actor

import akka.actor.{ Actor, ActorLogging, Props }

import scala.concurrent.Future
import scala.util.{ Failure, Success, Try }

//#user-case-classes
final case class User(name: String, age: Int, countryOfResidence: String)
final case class Users(users: Seq[User])
//#user-case-classes

import slick.jdbc.MySQLProfile.api._
import org.joda.time.DateTime
import com.github.tototoshi.slick.MySQLJodaSupport._

final case class ArcgisMaps(seq: Seq[ArcgisMap])
final case class ArcgisMap(uid: Option[Int], public: Boolean, title: String, height: Int, width: Int, extent: String, graphics: Option[String], city: Boolean, creation: Option[DateTime])

final case class ArcgisMapRow(tag: Tag) extends Table[(Option[Int], Boolean, String, Int, Int, String, Option[String], Boolean, Option[DateTime])](tag, "Map") {
  def uid = column[Option[Int]]("uid", O.PrimaryKey, O.AutoInc)
  def public = column[Boolean]("public", O.Default(true))
  def title = column[String]("title")
  def height = column[Int]("height")
  def width = column[Int]("width")
  def extent = column[String]("extent")
  def graphics = column[Option[String]]("graphics")
  def city = column[Boolean]("city")
  def creationDate = column[Option[DateTime]]("creationDate", O.SqlType("DATETIME"))

  def * = (uid, public, title, height, width, extent, graphics, city, creationDate)

}

/*
case class User(id: Option[Int], first: String, last: String)
class Users(tag: Tag) extends Table[User](tag, "users") {
  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
  def first = column[String]("first")
  def last = column[String]("last")
  def * = (id.?, first, last) <> (User.tupled, User.unapply)
}
val users = TableQuery[Users]
*/

object MapRegistryActor {
  final case class ActionPerformed(description: String)
  final case object GetMaps
  final case class CreateMap(arcgisMap: ArcgisMap)
  final case class GetMap(id: String)

  final case class DeleteMap(id: String)

  def props: Props = Props[MapRegistryActor]
}

class MapRegistryActor extends Actor with ActorLogging {
  import MapRegistryActor._
  import akka.pattern.pipe
  import scala.concurrent.ExecutionContext.Implicits.global

  val maps = TableQuery[ArcgisMapRow]

  /*
  val setup = DBIO.seq(
    maps.schema.create,
    maps += (None, true, "test", 333, 444, "{id: 33}", Some("<graphics>"), true, DateTime.now()),
    maps += (None, true, "test", 333, 444, "{id: 33}", Some("<graphics>"), true, DateTime.now()),
    maps += (None, true, "test", 333, 444, "{id: 33}", Some("<graphics>"), true, DateTime.now())
  )
  */
  val db = Database.forConfig("mysql")
  //  try {
  //val setupFuture = db.run(setup)
  //setupFuture.onComplete(println)
  //  } finally db.close()

  var users = Set.empty[User]

  def receive: Receive = {
    case GetMaps =>
      val res = db.run(maps.result).map(_.map(record => (ArcgisMap.apply _).tupled(record)))
      res.map(ArcgisMaps).pipeTo(sender)

    case CreateMap(map) =>
      // Convert map to tuple
      println("************", map)
      maps += (None, true, "test", 333, 444, "{id: 33}", Some("<graphics>"), true, Some(DateTime.now()))
      val insertAction = maps += ArcgisMap.unapply(map).get
      db.run(insertAction).map(i => println("--------", i))
      println("************")
      sender() ! ActionPerformed(s"Map ${map.uid} created.")

    case GetMap(id) =>
      Try(id.toInt).toOption match {
        case None => sender() ! None
        case Some(i) =>
          val q = maps.filter(_.uid === i)
          val resultSeq: Future[Seq[ArcgisMap]] = db.run(q.result).map(_.map(record => (ArcgisMap.apply _).tupled(record)))
          val result: Future[Option[ArcgisMap]] = resultSeq.map(_.headOption)
          result.pipeTo(sender)
      }

    case DeleteMap(idMap) =>
      Try(idMap.toInt) match {
        case Failure(_) => sender() ! ActionPerformed(s"Map id $idMap must be an integer")
        case Success(id) =>
          val q = maps.filter(_.uid === id)
          val affectedRows: Future[Int] = db.run(q.delete)
          affectedRows.onComplete {
            case Success(_) => sender() ! ActionPerformed(s"Map $id deleted")
            case Failure(e) => sender() ! ActionPerformed(e.getMessage)
          }
      }
  }
}
//#user-registry-actor