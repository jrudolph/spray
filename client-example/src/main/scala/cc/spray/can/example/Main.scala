/*
 * Copyright (C) 2011 Mathias Doenitz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cc.spray.can
package example

import akka.config.Supervision._
import akka.actor.{Supervisor, Actor}
import org.slf4j.LoggerFactory
import akka.dispatch.Future

object Main extends App {
  val log = LoggerFactory.getLogger(getClass)

  // start and supervise the HttpClient actor
  Supervisor(
    SupervisorConfig(
      OneForOneStrategy(List(classOf[Exception]), 3, 100),
      List(Supervise(Actor.actorOf(new HttpClient()), Permanent))
    )
  )

  import HighLevelHttpClient._
  val dialog: Future[HttpResponse] = HttpDialog("github.com").send(HttpRequest()).end

  dialog.onComplete { future =>
    future.value match {
      case Some(Right(response)) => show(response)
      case error => log.error("Error: {}", error)
    }
    Actor.registry.shutdownAll()
  }

  def show(response: HttpResponse) {
    log.info(
      """|Result from host:
         |status : {}
         |headers: {}
         |body   : {}""".stripMargin,
      Array(response.status: java.lang.Integer, response.headers, response.bodyAsString)
    )
  }
}