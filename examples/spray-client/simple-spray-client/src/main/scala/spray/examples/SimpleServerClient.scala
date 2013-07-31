package spray.examples

import spray.io._
import java.security.cert.X509Certificate
import javax.net.ssl.{KeyManager, SSLContext, X509TrustManager}
import akka.util.Timeout
import akka.actor._
import spray.httpx.RequestBuilding
import scala.concurrent.duration._
import akka.pattern.ask
import spray.can.Http.{HostConnectorInfo, HostConnectorSetup}
import spray.can.client.{HostConnectorSettings, ClientConnectionSettings}
import scala.concurrent.{Future, ExecutionContext}
import spray.http.{HttpMethods, HttpRequest, HttpResponse}
import spray.http.StatusCodes.Redirection

trait CustomSslConfiguration {
  implicit val trustfulSslContext: SSLContext = {
    object BlindFaithX509TrustManager extends X509TrustManager {
      def checkClientTrusted(chain: Array[X509Certificate], authType: String) = ()
      def checkServerTrusted(chain: Array[X509Certificate], authType: String) = ()
      def getAcceptedIssuers = Array[X509Certificate]()
    }

    val context = SSLContext.getInstance("TLS")
    context.init(Array[KeyManager](), Array(BlindFaithX509TrustManager), null)
    context
  }

  implicit val sslEngineProvider: ClientSSLEngineProvider = {
    ClientSSLEngineProvider { engine =>
      engine.setEnabledCipherSuites(Array("TLS_RSA_WITH_AES_256_CBC_SHA"))
      engine.setEnabledProtocols(Array("SSLv3", "TLSv1"))
      engine
    }
  }
}

object SimpleServerClient extends App with CustomSslConfiguration {
  implicit val system = ActorSystem()
  import system.dispatcher

  import akka.io.IO
  import spray.can.Http
  import spray.client.pipelining._

  implicit val timeout: Timeout = 5.seconds

  val httpClient = IO(Http)

  val maxRedirects = 5
  def sendReceiveWithRedirect(transport: ActorRef, depth: Int = 0)
                             (implicit ec: ExecutionContext, futureTimeout: Timeout): HttpRequest => Future[HttpResponse] =
    request =>
      sendReceive(transport)(ec, futureTimeout).apply(request).flatMap(
        response => {
          (response.status, response.headers.find(_.lowercaseName == "location")) match {
            case (_: Redirection, Some(location)) if depth < maxRedirects =>
              val redirect: HttpRequest => Future[HttpResponse] = sendReceiveWithRedirect(transport, depth + 1)(ec, futureTimeout)
              redirect(request.copy(uri = location.value, method = HttpMethods.GET))
            case _ =>
              Future.successful(response)
          }
        })

  (httpClient ? HostConnectorSetup("puredata.info", 443, sslEncryption = true)(system, sslEngineProvider)).mapTo[HostConnectorInfo].foreach { info =>
    println(s"Got connector: $info. Ssl encryption is ${info.setup.settings.get.connectionSettings.sslEncryption}")
    val pipeline = sendReceiveWithRedirect(info.hostConnector)

    pipeline(Get("/login_form")).onComplete { res =>
      println(res)
    }
  }

  system.scheduler.scheduleOnce(5.seconds) {
    system.shutdown()
  }
}
