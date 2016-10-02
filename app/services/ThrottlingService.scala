package services

import com.google.inject.Inject

import scala.concurrent.Future

trait ThrottlingServiceLike {
  def isRequestAllowed(token:Option[String]): Future[Boolean]
}

class ThrottlingService @Inject()(
  slaService: SlaServiceLike,
  rpsService: RpsServiceLike) extends ThrottlingServiceLike {

  val graceRps: Int = 10

  override def isRequestAllowed(token: Option[String]): Future[Boolean] = {

    token match {
      case Some(token) =>
        val ftr = slaService.getSlaByToken(token)
        for {
          sla <- ftr
          rps <- Future.successful(rpsService.get(token))
        } yield sla.rps > rps.count.longValue()
      case None =>
    }

    Future.successful(true)
  }
}