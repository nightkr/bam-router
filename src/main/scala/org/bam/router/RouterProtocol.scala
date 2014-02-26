package org.bam.router

import org.json4s._
import akka.io.{PipePair, PipelineContext, PipelineStage}

import RouterProtocol._

object RouterProtocol {
  type MessagePayload = JValue

  /**
   * A bunch of constraints for recipients of a message.
   *
   * An empty list for a constraint means that constraint is ignored, a list containing one or more elements
   * means that the client must match at least one of them. A client must fulfill all specified constraints
   * in order to be considered a match.
   */
  case class RecipientFilter(blids: Seq[Int]) {
    private def constraintFulfilled[A](elem: A, list: Seq[A]): Boolean =
      list.isEmpty || list.contains(elem)

    private def constraintFulfilledOpt[A](elemOpt: Option[A], list: Seq[A]): Boolean =
      elemOpt.exists(constraintFulfilled(_, list))

    def matches(client: ClientMetadata): Boolean = Seq(
      constraintFulfilledOpt(client.blid, blids)
    ).forall(identity)
  }

  case class ClientMetadata(blid: Option[Int])

  /**
   * Sent from API client (such as any Blockland instance) to the router.
   */
  sealed trait Incoming

  case class IncomingMessage(recipients: RecipientFilter, kind: String, payload: MessagePayload) extends Incoming

  /**
   * Sent to the API client from the router.
   */
  sealed trait Outgoing

  case class Message(sender: ClientMetadata, kind: String, payload: MessagePayload) extends Outgoing

  /**
   * Envelope for Outgoings, containing recipient info.
   */
  case class SendTo(outgoing: Outgoing, recipients: RecipientFilter)

  class RouterProtocolAdapterPipePair(ctx: PipelineContext) extends PipePair[Outgoing, JObject, Incoming, JObject] {
    override def eventPipeline: (JObject) => Iterable[Result] = ???

    override def commandPipeline: (Outgoing) => Iterable[Result] = ???
  }

}

class RouterProtocolAdapter extends PipelineStage[PipelineContext, Outgoing, JObject, Incoming, JObject] {
  override def apply(ctx: PipelineContext) = new RouterProtocolAdapterPipePair(ctx)
}
