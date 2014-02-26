package org.bam.router

import akka.actor._
import akka.io._
import java.net.InetSocketAddress
import akka.util.ByteString
import akka.io.TcpPipelineHandler.{Init, WithinActorContext}
import org.json4s.JsonAST.JObject
import akka.io.IO

class RouterManager extends Actor {
  override def preStart() {
    super.preStart()

    context.actorOf(Props[RouterTCPTransport], "tcp")
  }

  override def receive: Actor.Receive = PartialFunction.empty
}

class RouterTCPTransport extends Actor with ActorLogging with ActorSettings {
  // Kill connections on error
  override def supervisorStrategy: SupervisorStrategy = OneForOneStrategy() {
    case _: Exception => SupervisorStrategy.Stop
    case _: Error => SupervisorStrategy.Escalate
  }

  override def preStart() {
    import context._

    super.preStart()
    IO(Tcp) ! Tcp.Bind(context.self, new InetSocketAddress(settings.api.tcp.port))
  }

  override def receive: Actor.Receive = {
    case _: Tcp.Bound =>
    case Tcp.Connected(remote, local) =>
      val init = TcpPipelineHandler.withLogger(log,
        //new RouterProtocolAdapter >>
        new JSONAdapter >>
          new StringByteStringAdapter("latin1") >>
        new DelimiterFraming(maxSize = 1024, delimiter = ByteString('\n'), includeDelimiter = false) >>
        new TcpReadWriteAdapter >>
        new BackpressureBuffer(lowBytes = 100, highBytes = 1000, maxBytes = 10000)
      )
      val connection = context.sender
      val handler = context.actorOf(Props(new RouterTCPClientHandler(init)))
      val pipeline = context.actorOf(TcpPipelineHandler.props(init, connection, handler))
      connection ! Tcp.Register(pipeline)
  }
}

class RouterTCPClientHandler(pipeline: Init[WithinActorContext, JObject, JObject]) extends Actor {
  override def receive: Actor.Receive = {
    case pipeline.Event(input) =>
  }
}
