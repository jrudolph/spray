/**
 * Copyright (C) 2009-2013 Typesafe Inc. <http://www.typesafe.com>
 */

package akka.io

import java.io.IOException
import java.nio.channels.{ SelectionKey, SocketChannel }
import java.net.ConnectException
import scala.collection.immutable
import scala.concurrent.duration.Duration
import akka.actor.{ ReceiveTimeout, ActorRef }
import akka.io.Inet.SocketOption
import akka.io.TcpConnection.CloseInformation
import akka.io.SelectionHandler._
import akka.io.Tcp._

/**
 * An actor handling the connection state machine for an outgoing connection
 * to be established.
 *
 * INTERNAL API
 */
private[io] class TcpOutgoingConnection(_tcp: TcpExt,
                                        channelRegistry: ChannelRegistry,
                                        commander: ActorRef,
                                        connect: Connect)
    extends TcpConnection(_tcp, SocketChannel.open().configureBlocking(false).asInstanceOf[SocketChannel]) {

  import connect._

  context.watch(commander) // sign death pact

  localAddress.foreach(channel.socket.bind)
  options.foreach(_.beforeConnect(channel.socket))
  channelRegistry.register(channel, 0)
  timeout foreach context.setReceiveTimeout //Initiate connection timeout if supplied

  private def stop(): Unit = stopWith(CloseInformation(Set(commander), connect.failureMessage))

  private def reportConnectFailure(thunk: ⇒ Unit): Unit = {
    try {
      thunk
    } catch {
      case e: IOException ⇒
        log.debug("Could not establish connection to [{}] due to {}", remoteAddress, e)
        stop()
    }
  }

  def receive: Receive = {
    case registration: ChannelRegistration ⇒
      log.debug("Attempting connection to [{}]", remoteAddress)
      reportConnectFailure {
        if (channel.connect(remoteAddress))
          completeConnect(registration, commander, options)
        else {
          registration.enableInterest(SelectionKey.OP_CONNECT)
          context.become(connecting(registration, commander, options))
        }
      }
  }

  def connecting(registration: ChannelRegistration, commander: ActorRef,
                 options: immutable.Traversable[SocketOption]): Receive = {
    case ChannelConnectable ⇒
      reportConnectFailure {
        if (channel.finishConnect()) {
          if (timeout.isDefined) context.setReceiveTimeout(Duration.Undefined) // Clear the timeout
          log.debug("Connection established to [{}]", remoteAddress)
          completeConnect(registration, commander, options)
        } else {
          log.debug("Could not establish connection because finishConnect didn't return true")
          stop()
        }
      }

    case ReceiveTimeout ⇒
      if (timeout.isDefined) context.setReceiveTimeout(Duration.Undefined) // Clear the timeout
      log.debug("Connect timeout expired, could not establish connection to {}", remoteAddress)
      stop()
  }
}
