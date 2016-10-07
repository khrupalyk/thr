package modules

import javax.inject.{Inject, Provider}

import akka.actor.ActorSystem
import com.google.inject.AbstractModule
import com.typesafe.config.{ConfigFactory, Config}
import play.api.libs.concurrent.AkkaGuiceSupport
import scheduler.{MessageProducer, Scheduler}

class SystemModule extends AbstractModule with AkkaGuiceSupport {
  import SystemModule._

  def configure(): Unit = {

    bind(classOf[Scheduler])
      .toProvider(classOf[SchedulerProvider])
      .asEagerSingleton()

  }
}

object SystemModule {

  class SchedulerProvider @Inject() (actorySystem: ActorSystem) extends Provider[Scheduler] {
    val get = Scheduler(actorySystem, ConfigFactory.load())
  }
}

