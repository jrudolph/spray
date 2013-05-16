package spray.swagger

import spray.routing._
import reflect.macros.Context

object SwaggerSupportMacro {
  def apiDescriptionImpl(c: Context)(route: c.Expr[Route]): c.Expr[Route] = {
    import c._
    import c.universe._
    import Directives._

    reify(route.splice ~ get { path("tester") { complete("tester") } })
  }
}

object SwaggerSupport {
  def withApiDescription(route: Route): Route = macro SwaggerSupportMacro.apiDescriptionImpl
}
