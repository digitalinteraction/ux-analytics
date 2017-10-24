package uk.ac.ncl.openlab.uxAnalytics.server

import java.time.ZonedDateTime

import anorm.{Macro, SQL}
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._
import uk.ac.ncl.openlab.uxAnalytics.db.DataBaseConnection

import scala.util.{Failure, Success}

/**
  * Created by Tim Osadchiy on 20/10/2017.
  */
object TestDb extends App {

  private case class Test(name: String, age: Int)

  case class Ev(event_category: String, event_name: String, data: String, created: ZonedDateTime)

  private val t = Test("Andrew", 12)

  val q =
    """
      |INSERT INTO ux_data (event_category, event_name, data)
      |VALUES ({event_category}, {event_name}, {data}::JSON);
    """.stripMargin

  val sel = "SELECT event_category, event_name, data::TEXT FROM ux_data;"

  put

  def put = {
    DataBaseConnection.tryWithConnection { implicit conn =>
      SQL(q).on('event_category -> "test3", 'event_name -> "test3", 'data -> t.asJson.noSpaces).execute()
      Success(())
    } match {
      case Failure(e) => e.printStackTrace()
      case Success(_) => println("ZBS")
    }
  }

  def get = {
    DataBaseConnection.tryWithConnection { implicit conn =>
      val r = SQL(sel).as(Macro.namedParser[Ev].*)
      Success(r)
    } match {
      case Failure(e) => e.printStackTrace()
      case Success(r) => r.foreach(el => println(decode[Test](el.data)))
    }
  }

}
