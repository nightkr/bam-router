package org.bam.router

import akka.io.{SymmetricPipePair, PipePair, SymmetricPipelineStage, PipelineContext}
import org.json4s._
import org.json4s.jackson.JsonMethods._

class JSONAdapter extends SymmetricPipelineStage[PipelineContext, JObject, String] {
  override def apply(ctx: PipelineContext): PipePair[JObject, String, JObject, String] = new JSONAdapter.JSONPipePair(ctx)
}

object JSONAdapter {

  class JSONPipePair(ctx: PipelineContext) extends SymmetricPipePair[JObject, String] {
    override def eventPipeline: (String) => Iterable[Result] = {
      s =>
        ctx.singleEvent(parse(s).asInstanceOf[JObject])
    }

    override def commandPipeline: (JObject) => Iterable[Result] = {
      json =>
        ctx.singleCommand(compact(json) + "\n")
    }
  }

}