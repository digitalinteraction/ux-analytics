package uk.ac.ncl.openlab.uxAnalytics.classes

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

import io.circe.syntax._
import io.circe.{Decoder, Encoder, Json}

import scala.util.{Failure, Success}

/**
  * Created by Tim Osadchiy on 25/10/2017.
  */
object encoders {
  implicit val dateTimeEncoder: Encoder[ZonedDateTime] = Encoder.instance(a => DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(a).asJson)
  implicit val dateTimeDecoder: Decoder[ZonedDateTime] = Decoder.decodeString.emapTry {
    str =>
      try {
        Success(ZonedDateTime.parse(str))
      } catch {
        case e: Exception => Failure(e)
      }
  }

  implicit final class JsonResponseEncoder[A](val wrappedEncodeable: A) extends AnyVal {
    final def asJsonResponse(implicit encoder: Encoder[A]): String = wrappedEncodeable.asJson.noSpaces
  }

}
