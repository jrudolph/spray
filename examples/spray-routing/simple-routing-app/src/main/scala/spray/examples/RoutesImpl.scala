package spray.examples

import spray.routing.{ Directives => D }

object RoutesImpl {
  def get(inner: Array[Route]): Route =
    D.get(chain(inner)).toJavaRoute

  def path(pattern: String, inner: Array[Route]): Route =
    D.path(pattern)(chain(inner)).toJavaRoute

  def handle(handler: Handler): Route =
    spray.routing.Route { ctx =>
      val javaCtx = new RequestContext {
        def get[T](extraction: Extraction[T]): T = {
          extraction.toScala.directive { value =>
            ctx => return value
          }(ctx)
          ???
        }

        def complete(value: String): Unit = ctx.complete(value)
        def complete[T](value: T, marshaller: Marshaller[T]): Unit = ???
      }
      handler.handle(javaCtx)
    }.toJavaRoute

  def complete(staticValue: String): Route =
    D.complete(staticValue).toJavaRoute

  def chain(inner: Array[Route]): spray.routing.Route = {
    import spray.routing.RouteConcatenation._
    inner.map(_.toScalaRoute).reduce(_ ~ _)
  }
}
