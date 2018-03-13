package ch.hepia.abaplans.server

import ch.hepia.abaplans.server.MapRegistryActor.ActionPerformed
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import spray.json.{ JsArray, JsObject, JsString, JsValue, RootJsonFormat }

//#json-support
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.DefaultJsonProtocol

trait JsonSupport extends SprayJsonSupport {

  // import the default encoders for primitive types (Int, String, Lists etc)
  import DefaultJsonProtocol._

  implicit object DateTimeFormat extends RootJsonFormat[DateTime] {

    val formatter = ISODateTimeFormat.basicDateTimeNoMillis

    def write(obj: DateTime): JsValue = {
      JsString(formatter.print(obj))
    }

    def read(json: JsValue): DateTime = json match {
      case JsString(s) => {
        formatter.parseDateTime(s)
      }
    }

    /*
    def error(v: Any): DateTime = {
      val example = formatter.print(0)
      deserializationError(f"'$v' is not a valid date value. Dates must be in compact ISO-8601 format, e.g. '$example'")
    }
    */
  }

  implicit val arcgisMapJsonFormat = jsonFormat9(ArcgisMap)

  //implicit val dateTimeJsonFormat = jsonFormat1(DateTime)
  implicit val arcgisMapsJsonFormat = jsonFormat1(ArcgisMaps)

  implicit val userJsonFormat = jsonFormat3(User)
  implicit val usersJsonFormat = jsonFormat1(Users)

  implicit val actionPerformedJsonFormat = jsonFormat1(ActionPerformed)
}
//#json-support
