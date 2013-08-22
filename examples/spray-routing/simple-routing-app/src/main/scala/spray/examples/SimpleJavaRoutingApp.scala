package spray.examples

import akka.actor.ActorSystem
import spray.routing.SimpleRoutingApp

object SimpleJavaRoutingApp extends SimpleRoutingApp {
  implicit val system = ActorSystem()

  def run(interface: String, port: Int, route: Route): Unit = {
    startServer(interface, port)(route.toScalaRoute)
  }
}
