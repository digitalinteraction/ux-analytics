package uk.ac.ncl.openlab.uxAnalytics.db

import java.time.ZonedDateTime

import anorm.{Macro, SQL}
import uk.ac.ncl.openlab.uxAnalytics.classes.UxEvent
import io.circe.parser._

import scala.util.{Failure, Success, Try}

/**
  * Created by Tim Osadchiy on 24/10/2017.
  */
object UxEventRepo {

  private case class UxEventRow(id: Long, event_category: String, event_name: String, data: String, created: ZonedDateTime) {
    def toEvent: Try[UxEvent] = {
      parse(data) match {
        case Left(e) => Failure(e)
        case Right(json) => Success(UxEvent(Some(id), event_category, event_name, json, Some(created)))
      }
    }
  }

  private val tableName = "ux_data"
  private val returningFields = "id, event_category, event_name, data::TEXT, created"
  private val insertQ =
    s"""
       |INSERT INTO $tableName (event_category, event_name, data)
       |VALUES ({event_category}, {event_name}, {data}::JSON)
       |RETURNING $returningFields;
    """.stripMargin
  private val selectQ = s"SELECT $returningFields FROM $tableName;"

  def put(uxEvent: UxEvent): Try[UxEvent] = {
    DataBaseConnection.tryWithConnection { implicit conn =>
      val r = SQL(insertQ).on('event_category -> uxEvent.eventCategory,
        'event_name -> uxEvent.eventName,
        'data -> uxEvent.data.noSpaces)
        .executeQuery().as(Macro.namedParser[UxEventRow].single)
      r.toEvent
    }
  }

  def get: Try[Seq[UxEvent]] = {
    DataBaseConnection.tryWithConnection { implicit conn =>
      val r = SQL(selectQ).as(Macro.namedParser[UxEventRow].*)
      val col = r.map(_.toEvent).collect { case Success(x) => x }
      Success(col)
    }
  }

}
