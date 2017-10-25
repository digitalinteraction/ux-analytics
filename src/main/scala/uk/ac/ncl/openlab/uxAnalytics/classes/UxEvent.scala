package uk.ac.ncl.openlab.uxAnalytics.classes

import java.time.ZonedDateTime

import io.circe.Json
import io.circe.syntax._

/**
  * Created by Tim Osadchiy on 24/10/2017.
  */

case class UxEvent(id: Option[Long], eventCategory: String, eventName: String, data: Json, created: Option[ZonedDateTime])

object UxEvent {

  def apply(id: Option[Long], eventCategory: String, eventName: String, data: Json, created: Option[ZonedDateTime]): UxEvent =
    new UxEvent(id, eventCategory, eventName, data, created)

  def apply(eventCategory: String, eventName: String, data: Json): UxEvent =
    new UxEvent(None, eventCategory, eventName, data.asJson, None)
}
