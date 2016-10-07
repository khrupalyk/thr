package services

import com.google.inject.{ImplementedBy, Inject}
import models.Sla
import scheduler.{MessageProducer, Scheduler}

import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[ThrottlingService])
trait ThrottlingServiceLike {
  def isRequestAllowed(token:Option[String]): Future[Boolean]
}

class ThrottlingService @Inject()(
  slaService: SlaServiceLike,
  rpsService: RpsServiceLike,
  scheduler: Scheduler,
  slaCache: SlaCacheLike,
  implicit val ex: ExecutionContext) extends ThrottlingServiceLike {

  val graceRps: Int = 1

  private val producer = scheduler.register("activemq:CHANGE_SLA", {
    case (user: String, newRps: Int) =>
      slaService.setSla(user, newRps)
  })

  override def isRequestAllowed(maybeToken: Option[String]): Future[Boolean] = {
    maybeToken match {
      case Some(token) =>
        getSla(token) map { sla =>
          val rps = rpsService.get(sla.user)
          if(sla.rps > rps.count.longValue()) {
            true
          } else {
            producer.produce(MessageProducer.PredefinedMessage(sla.user, (sla.user, rps.count.intValue()*10/100), 10000))
            false
          }
        }
      case None =>
        Future {
          val rps = rpsService.get("unknown")
          rps.count.longValue() < graceRps
        }
    }
  }

  private def getSla(token: String) = {
    slaCache.get(token)
      .map(Future.successful)
      .getOrElse(
        slaService.getSlaByToken(token) map { sla =>
          slaCache.put(token, sla)
          sla
        }
      )
  }
}