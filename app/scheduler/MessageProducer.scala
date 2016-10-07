package scheduler

import java.util

import akka.actor.ActorSystem
import org.apache.activemq.ScheduledMessage
import org.apache.camel.ProducerTemplate
import scheduler.MessageProducer.Message
import collection.JavaConversions.mapAsJavaMap

class MessageProducer(actorSystem: ActorSystem, endpointUrl: String, template: ProducerTemplate, q: util.Set[String]) {

  def produce(msg: MessageProducer.PredefinedMessage): Unit = {
    if(!q.contains(msg.user)) {
      q.add(msg.user)
      template.sendBodyAndHeaders(endpointUrl, Message(msg.user, msg.message), mapAsJavaMap(msg.paramsAsMap))
    }
  }

}

object MessageProducer {

  def apply(actorSystem: ActorSystem, endpointUrl: String, template: ProducerTemplate, q: util.Set[String]): MessageProducer = {
    new MessageProducer(actorSystem, endpointUrl, template, q)
  }

  case class PredefinedMessage(user: String, message: AnyRef, delay: Long) {
    val paramsAsMap: Map[String, AnyRef] = Map(ScheduledMessage.AMQ_SCHEDULED_DELAY -> delay).asInstanceOf[Map[String, AnyRef]]
  }

  case class Message(user: String, message: AnyRef)
}
