package spray.examples

import spray.routing.Directives

trait ExtractionScalaSide[T] extends Extraction[T] {
  def directive: spray.routing.Directive1[T]

  def get(ctx: RequestContext): T = ctx.get(this)
}

object Extractions {
  import Directives._
  def intParameter(name: String): Extraction[Integer] = new ExtractionScalaSide[Integer] {
    def directive: spray.routing.Directive1[Integer] = parameter(name.as[Int]).asInstanceOf[spray.routing.Directive1[Integer]]
  }
}


