package org.bam.router

import akka.kernel.Bootable
import akka.actor.{Props, ActorSystem}

class Kernel extends Bootable {
  val system = ActorSystem("bam-router")

  override def startup(): Unit = {
    system.actorOf(Props[RouterManager], "router")
  }

  override def shutdown(): Unit = {
    system.shutdown()
  }
}

object KernelMain extends App {
  akka.kernel.Main.main(Array(classOf[Kernel].getCanonicalName))
}
