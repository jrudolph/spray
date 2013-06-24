package docs.directives

import org.specs2.mutable.Specification
import spray.testkit.Specs2RouteTest
import spray.routing.{HttpService, Directives}
import spray.http.HttpHeaders.{Accept, RawHeader}
import spray.http.StatusCodes._
import spray.http.MediaTypes

class HeaderDirectivesExamplesSpec extends Specification with Specs2RouteTest with HttpService {
  def actorRefFactory = system

  "example-1" in {
    val route =
      headerValueByName("X-User-Id") { userId =>
        complete(s"The user is $userId")
      }

    Get("/") ~> RawHeader("X-User-Id", "Joe42") ~> route ~> check {
      entityAs[String] === "The user is Joe42"
    }

    Get("/") ~> sealRoute(route) ~> check {
      status === BadRequest
      entityAs[String] === "Request is missing required HTTP header 'X-User-Id'"
    }
  }
}
