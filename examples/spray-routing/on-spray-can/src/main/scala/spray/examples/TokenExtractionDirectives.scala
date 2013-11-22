package spray.examples

import java.util.concurrent.atomic.AtomicLong
import shapeless.{HNil, ::}
import spray.http.{Rendering, HttpHeader, HttpHeaders}
import spray.routing._

trait Token[T] extends Directive1[T]

trait TokenExtractionDirectives { self: Directives =>
  import TokenExtractionDirectives._

  def extractAsToken[T](extractor: RequestContext => T): Directive1[Token[T]] = {
    val token = Token[T]()
    new Directive1[Token[T]] {
      def happly(f: Token[T] :: HNil => Route): Route = {
        val inner = f(token :: HNil) // construct inner route during request-building time
        ctx => {
          val value = extractor(ctx)
          val newCtx =
            ctx.withRequestMapped(_.mapHeaders(new TokenValue(token, value) :: _))
          inner(newCtx)
        }
      }
    }
  }
}

private object TokenExtractionDirectives {
  object Token {
    def apply[T](): Token[T] =
      new Token[T] {
        val dir = Directives.headerValuePF {
          case t: TokenValue[T] if t.key eq this => t.storedValue
        }
        def happly(f: T :: HNil => Route): Route = dir.happly(f)
      }
  }

  class TokenValue[T](val key: Token[T], val storedValue: T) extends HttpHeader {
    def name: String = "TokenValue"
    def value: String = ???
    def lowercaseName: String = "tokenvalue"
    def render[R <: Rendering](r: R): r.type = r ~~ name
  }
}
