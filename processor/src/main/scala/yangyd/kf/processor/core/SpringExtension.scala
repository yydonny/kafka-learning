package yangyd.kf.processor.core

import akka.actor.{Actor, ExtendedActorSystem, Extension, ExtensionId, ExtensionIdProvider}
import org.springframework.context.ApplicationContext

/**
  * Equip an actor with the reference to Spring application context.
  */
trait ApplicationContextSupport {
  _: Actor ⇒
  def applicationContext: ApplicationContext = SpringExtension(context.system).applicationContext
}

class SpringExtensionImpl extends Extension {
  @volatile var applicationContext: ApplicationContext = _
}

object SpringExtension extends ExtensionId[SpringExtensionImpl] with ExtensionIdProvider {
  override def createExtension(system: ExtendedActorSystem) = new SpringExtensionImpl
  override def lookup(): ExtensionId[_ <: Extension] = SpringExtension
}
