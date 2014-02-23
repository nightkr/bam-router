package org.bam.router

import akka.actor.{ActorLogging, Props, Actor}
import akka.io._
import java.net.InetSocketAddress
import akka.util.ByteString
import akka.io.TcpPipelineHandler.{Init, WithinActorContext}

class RouterManager extends Actor {
  override def preStart() {
    super.preStart()

    context.actorOf(Props[RouterTCPTransport], "tcp")
  }

  override def receive: Actor.Receive = PartialFunction.empty
}

class RouterTCPTransport extends Actor with ActorLogging {
  override def preStart() {
    import context._

    super.preStart()
    IO(Tcp) ! Tcp.Bind(context.self, new InetSocketAddress(6767))
  }

  override def receive: Actor.Receive = {
    case _: Tcp.Bound =>
    case Tcp.Connected(remote, local) =>
      val init = TcpPipelineHandler.withLogger(log,
        new StringByteStringAdapter("latin1") >>
        new DelimiterFraming(maxSize = 1024, delimiter = ByteString('\n'), includeDelimiter = false) >>
        new TcpReadWriteAdapter >>
        new BackpressureBuffer(lowBytes = 100, highBytes = 1000, maxBytes = 10000)
      )
      val connection = context.sender
      val handler = context.actorOf(Props[RouterTCPClientHandler])
      val pipeline = context.actorOf(TcpPipelineHandler.props(init, connection, handler))
      connection ! Tcp.Register(pipeline)
  }
}

class RouterTCPClientHandler(pipeline: Init[WithinActorContext, String, String]) extends Actor {
  override def receive: Actor.Receive = PartialFunction.empty
}

object RouterProtocol {
  case class RecipientFilter(blids: Seq[Int])
  case class IncomingMessage(recipients: RecipientFilter, kind: String, payload: AnyRef)
}
