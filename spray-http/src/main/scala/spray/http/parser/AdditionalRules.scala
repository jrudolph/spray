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

package spray.http
package parser

import org.parboiled.scala._
import BasicRules._
import java.net.InetAddress

// implementation of additional parsing rules required for extensions that are not in the core HTTP standard
private[parser] trait AdditionalRules {
  this: Parser ⇒

  def Ip: Rule1[InetAddress] = rule(
    IpNumberByte ~ ch('.') ~ IpNumberByte ~ ch('.') ~ IpNumberByte ~ ch('.') ~ IpNumberByte ~~>
      ((i1, i2, i3, i4) ⇒ InetAddress.getByAddress(Array[Byte](i1, i2, i3, i4))) ~ OptWS)

  def IpNumberByte: Rule1[Byte] = rule {
    group(Digit ~ optional(Digit ~ optional(Digit))) ~> (_.toInt.toByte)
  }

  def Challenge = rule {
    AuthScheme ~ zeroOrMore(AuthParam, separator = ListSep) ~~> { (scheme, params) ⇒
      val (realms, otherParams) = params.partition(_._1 == "realm")
      HttpChallenge(scheme, realms.headOption.map(_._2).getOrElse(""), otherParams.toMap)
    }
  }

  def AuthScheme = rule {
    Token ~ OptWS
  }

  def AuthParam = rule {
    Token ~ "=" ~ (Token | QuotedString) ~~> ((_, _))
  }
}