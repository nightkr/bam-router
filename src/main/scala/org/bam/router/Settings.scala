package org.bam.router

import com.typesafe.config.Config
import akka.actor._

object Settings extends ExtensionId[SettingsImpl] with ExtensionIdProvider {
  override def lookup() = Settings

  override def createExtension(system: ExtendedActorSystem) = new SettingsImpl(system.settings.config)
}

class SettingsImpl(config: Config) extends Extension {
  private val c = config.getConfig("bam.router")

  object api {
    class Transport(transportConfig: Config) {
      val port = transportConfig.getInt("port")
    }
    object tcp extends Transport(c.getConfig("api.tcp"))
  }
}

trait ActorSettings { this: Actor =>
  lazy val settings = Settings(context.system)
}
