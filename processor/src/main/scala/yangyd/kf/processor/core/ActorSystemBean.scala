package yangyd.kf.processor.core

import javax.annotation.PreDestroy

import akka.actor.{ActorRef, ActorSystem, Inbox, Props}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Component

@Component
class ActorSystemBean @Autowired() (applicationContext: ApplicationContext) {
  private val system = ActorSystem("kf-processor")

  // make spring available to actors.
  // see yangyd.kf.processor.core.ApplicationContextSupport
  SpringExtension(system).applicationContext = applicationContext

  @PreDestroy
  def shutdown(): Unit = system.terminate()

  def createInbox(): Inbox = Inbox.create(system)

  def createActor(props: Props, name: String): ActorRef = system.actorOf(props, name)
}
