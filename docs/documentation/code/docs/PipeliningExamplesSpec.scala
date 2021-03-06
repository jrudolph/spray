package docs

import org.specs2.mutable.Specification


class PipeliningExamplesSpec extends Specification {

  "example-1" in {
    import akka.actor.ActorSystem
    import cc.spray.io.IOBridge

    val system = ActorSystem()

    val ioBridge = new IOBridge(system).start()

    // start applications actors, use ioBridge instance
    // ...

    system.registerOnTermination {
      // stop the IOBridge when the ActorSystem is shut down
      ioBridge.stop()
    }
  }

}
