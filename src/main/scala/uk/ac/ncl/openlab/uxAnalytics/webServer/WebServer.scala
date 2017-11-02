package uk.ac.ncl.openlab.uxAnalytics.webServer

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import io.circe.Json
import io.circe.generic.auto._
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import uk.ac.ncl.openlab.uxAnalytics.classes.UxEvent
import uk.ac.ncl.openlab.uxAnalytics.db.UxEventRepo
import uk.ac.ncl.openlab.uxAnalytics.classes.encoders._

import scala.io.StdIn
import scala.util.{Failure, Success}

/**
  * Created by Tim Osadchiy on 24/10/2017.
  */


case class EmitRequest(eventCategories: Seq[String], eventName: String, data: Json)

object WebServer {

  private final val config = ConfigFactory.load()
  private final val port = config.getInt("web.port")
  private final val secretToken = config.getString("web.token")
  private final val secretTokenName = config.getString("web.tokenName")

  private def createRoute(httpMethod: HttpMethod, rotePath: String)(route: Route) = {
    method(httpMethod) {
      path(rotePath) {
        route
      }
    }
  }

  private def createSecureRoute(httpMethod: HttpMethod, rotePath: String)(route: Route) = {
    createRoute(httpMethod, rotePath) {
      headerValueByName(secretTokenName) { token =>
        if (token == secretToken) {
          route
        } else {
          complete(HttpResponse(StatusCodes.Forbidden))
        }
      }
    }
  }

  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem = ActorSystem()
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    implicit val executionContext = system.dispatcher

    val routes =
      createSecureRoute(HttpMethods.POST, "emit") {
        entity(as[EmitRequest]) { emitRequest =>
          val ev = UxEvent(emitRequest.eventCategories, emitRequest.eventName, emitRequest.data)
          val res = UxEventRepo.put(ev) match {
            case Failure(e) => HttpResponse(StatusCodes.BadRequest, entity = e.getMessage)
            case Success(newEvent) => HttpResponse(StatusCodes.OK,
              entity = HttpEntity(ContentTypes.`application/json`, newEvent.asJsonResponse))
          }
          complete(res)
        }
      }

    val bindingFuture = Http().bindAndHandle(routes, "localhost", port)
    println(s"Server online at http://localhost:$port/\nPress RETURN to stop...")
    StdIn.readLine("Hit ENTER to exit")
    system.terminate()
    bindingFuture
      .flatMap(_.unbind())
      .onComplete(_ => system.terminate())

  }

}
