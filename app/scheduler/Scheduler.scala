package scheduler

import java.util
import java.util.Collections

import akka.actor.{ActorLogging, ActorSystem, Props, Status}
import akka.pattern.pipe
import akka.camel.{Ack, Camel, CamelExtension, CamelMessage, Consumer}
import com.typesafe.config.Config
import org.apache.activemq.camel.component.ActiveMQComponent
import scheduler.MessageProducer.Message
import scheduler.Scheduler.ConsumerBuilder

import scala.concurrent.Future

class Scheduler(camel: Camel, actorSystem: ActorSystem) {

  val queue = Collections.synchronizedSet(new util.TreeSet[String]())

  def register(endpoint: String, handle: Any => Future[Any], workers: Int = 1): MessageProducer = {
    val cons = new ConsumerBuilder(endpoint, handle, m => queue.remove(m.user))
    val props = cons.props
    0.until(workers).foreach { _ =>
      actorSystem.actorOf(props)
    }
    MessageProducer(actorSystem, endpoint, camel.template, queue)
  }
}

object Scheduler {

  private case class ConsumerBuilder(
    endpoint: String,
    handler: Any => Future[Any],
    end: Message => Unit) {

    def props = {
      Props(new Consumer with ActorLogging {

        override def autoAck = false
        val endpointUri: String = endpoint
        import context.dispatcher

        val receive: Receive = {
          case CamelMessage(msg: MessageProducer.Message, _) =>
            context.become({
              case Scheduler.Processed =>
                sender ! Ack
                context.unbecome()
              case f: Status.Failure =>
                sender ! f
                log.error(f.cause, "Can't process message due to an error")
                context.unbecome()
            }, false)
            val ftr = handler(msg.message).andThen {
              case _ => end(msg)
            }
            pipe(ftr.map(_ => Scheduler.Processed)).to(self, sender)
        }
      })
    }
  }


  def apply(actorSystem: ActorSystem, config: Config) = {
    val camel: Camel = CamelExtension(actorSystem)
    val component = ActiveMQComponent.activeMQComponent(config.getString("scheduler.activemq"))
    component.setTrustAllPackages(true)
    camel.context.addComponent("activemq", component)
    new Scheduler(camel, actorSystem)
  }

  private case object Processed
}
