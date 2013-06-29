package spray.examples

import scala.util.{Failure, Success}
import akka.actor.ActorSystem
import akka.event.Logging

object Main extends App
  with ConnectionLevelApiDemo
  with HostLevelApiDemo
  with RequestLevelApiDemo {

  // we always need an ActorSystem to host our application in
  implicit val system = ActorSystem("simple-example")
  import system.dispatcher // execution context for future transformations below
  val log = Logging(system, getClass)

  // the spray-can client-side API has three levels (from lowest to highest):
  // 1. the connection-level API
  // 2. the host-level API
  // 3. the request-level API
  //
  // this example demonstrates all three APIs by retrieving the server-version
  // of http://spray.io in three different ways

  val host = "spray.io"

  import Spore.{spore, capture}

  val result =
    demoConnectionLevelApi(host).flatMap {
      spore {
        val nextFunc = this.demoHostLevelApi(_)

        result1 => nextFunc(capture(host)).flatMap {
          spore {
            // I already have to assume here that when nesting spores it is
            // allowed to access things from out of the next level spore
            // which opens new loopholes for bugs.
            val nextFunc = this.demoRequestLevelApi(_)

            result2 =>
              for (result3 <- nextFunc(capture(host)))
                yield Set(capture(result1), capture(result2), capture(result3))
          }
        }
      }
    }

  result onComplete {
    spore {

      {
        case Success(res) => capture(log).info("{} is running {}", capture(host), res mkString ", ")
        case Failure(error) => capture(log).warning("Error: {}", error)
      }
    }
  }
  result onComplete {
    spore {
      _ => capture(system).shutdown()
    }
  }
}


trait Spore0[+R] extends Function0[R]
trait Spore1[-T, +R] extends Function1[T, R]

object Spore {
  def spore[R](f: () => R): Spore0[R] = new Spore0[R] {
    def apply(): R = f()
  }
  def spore[T, R](f: T => R): Spore1[T, R] = new Spore1[T, R] {
    def apply(v1: T): R = f(v1)
  }
  def capture[T](v: T): T = v
}
