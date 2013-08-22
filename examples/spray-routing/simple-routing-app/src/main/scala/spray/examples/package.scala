package spray

package object examples {
  implicit class ScalaSide(route: Route) {
    def toScalaRoute: spray.routing.Route = route.asInstanceOf[spray.routing.Route]
  }
  implicit class JavaSide(route: spray.routing.RequestContext => Unit) {
    def toJavaRoute: Route = new (spray.routing.RequestContext => Unit) with Route {
      def apply(ctx: routing.RequestContext): Unit = route(ctx)
    }
  }
  implicit class ScalaSideExtraction[T](extraction: Extraction[T]) {
    def toScala: ExtractionScalaSide[T] = extraction.asInstanceOf[ExtractionScalaSide[T]]
  }
}
