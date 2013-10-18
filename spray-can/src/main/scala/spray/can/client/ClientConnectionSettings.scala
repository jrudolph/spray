/*
 * Copyright © 2011-2013 the spray project <http://spray.io>
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

package spray.can.client

import com.typesafe.config.Config
import scala.concurrent.duration.Duration
import akka.actor.ActorRefFactory
import spray.can.parsing.ParserSettings
import spray.util._

case class ClientConnectionSettings(
    userAgentHeader: String,
    idleTimeout: Duration,
    requestTimeout: Duration,
    reapingCycle: Duration,
    responseChunkAggregationLimit: Int,
    chunklessStreaming: Boolean,
    requestHeaderSizeHint: Int,
    connectingTimeout: Duration,
    parserSettings: ParserSettings,
    proxySettings: Map[String, ProxySettings]) {

  requirePositive(idleTimeout)
  requirePositive(requestTimeout)
  requirePositive(reapingCycle)
  require(0 <= responseChunkAggregationLimit && responseChunkAggregationLimit <= Int.MaxValue,
    "response-chunk-aggregation-limit must be >= 0 and <= Int.MaxValue")
  require(0 <= requestHeaderSizeHint && requestHeaderSizeHint <= Int.MaxValue,
    "request-size-hint must be >= 0 and <= Int.MaxValue")
  requirePositive(connectingTimeout)
}

object ClientConnectionSettings extends SettingsCompanion[ClientConnectionSettings]("spray.can.client") {
  def fromSubConfig(c: Config) = {
    if (c.hasPath("ssl-encryption"))
      throw new IllegalArgumentException(
        "spray.can.client.ssl-encryption not supported any more. " +
          "Use Http.Connect(sslEncryption = true) to enable ssl encryption for a connection.")

    apply(
      c getString "user-agent-header",
      c getDuration "idle-timeout",
      c getDuration "request-timeout",
      c getDuration "reaping-cycle",
      c getIntBytes "response-chunk-aggregation-limit",
      c getBoolean "chunkless-streaming",
      c getIntBytes "request-header-size-hint",
      c getDuration "connecting-timeout",
      ParserSettings fromSubConfig c.getConfig("parsing"),
      ProxySettings fromSubConfig c.getConfig("proxy"))
  }

  def apply(optionalSettings: Option[ClientConnectionSettings])(implicit actorRefFactory: ActorRefFactory): ClientConnectionSettings =
    optionalSettings getOrElse apply(actorSystem)
}
