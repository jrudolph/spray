package spray.example.routing2

import spray.routing._
import shapeless._
import akka.actor.ActorSystem
import spray.routing.directives.BasicDirectives._
import spray.routing.RequestContext
import spray.util._
import spray.http.HttpHeader

object Main extends App with SimpleRoutingApp {
  implicit val system = ActorSystem("simple-routing-app")

  startServer("localhost", port = 8080) {
    get {
      path("") {
        ExtractionsV2.headerValueByName("User-Agent") { agent =>
          ExtractionsV2.extractHere { implicit ctx =>
            complete("The User-Agent is: "+agent.get)
          }
        }
      }
    }
  }
}

object ExtractionsV2 {
  trait ExtractionContext {
    def requestContext: RequestContext
  }
  trait Extract[+T] {
    def get(implicit ctx: ExtractionContext): T
  }
  object Extract {
    def add[T](value: RequestContext => T): (RequestContext => RequestContext, Extract[T]) = {
      val ex = new Extract[T] {
        def get(implicit ctx: ExtractionContext): T = ctx.requestContext.extractions(System.identityHashCode(this)).asInstanceOf[T]
      }
      (ctx => ctx.copy(extractions = ctx.extractions.updated(System.identityHashCode(ex), value(ctx))), ex)
    }

    implicit def autoExtract[T](e: Extract[T])(implicit ctx: ExtractionContext): T = e.get
  }
  def headerValueByName(string: String)(innerCreator: Extract[String] => Route): Route = {
    /*headerValue(optionalValue(headerName.toLowerCase))

    // FIXME: how to incorporate all of this as well:

    val protectedF: HttpHeader ⇒ Option[Either[Rejection, T]] = header ⇒
      try f(header).map(Right.apply)
      catch {
        case NonFatal(e) ⇒ Some(Left(MalformedHeaderRejection(header.name, e.getMessage.nullAsEmpty, Some(e))))
      }
    extract(_.request.headers.mapFind(protectedF)).flatMap {
      case Some(Right(a))        ⇒ provide(a)
      case Some(Left(rejection)) ⇒ reject(rejection)
      case None                  ⇒ reject
    }*/
    val lowerCaseName = string.toLowerCase
    def getValue: RequestContext => String =
      _.request.headers.mapFind {
        case HttpHeader(`lowerCaseName`, value) ⇒ Some(value)
        case _ => None
      }.get

    val (adder, ex) = Extract.add(getValue)
    val innerRoute = innerCreator(ex)

    ctx => innerRoute(adder(ctx))
  }
  def extractHere: Directive1[ExtractionContext] =
    extract(ctx => new ExtractionContext {
      def requestContext: RequestContext = ctx
    })
}

