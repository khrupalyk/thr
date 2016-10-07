package services

import com.google.inject.{ImplementedBy, Inject}
import models.Sla

import scala.concurrent.Future

@ImplementedBy(classOf[MockSlaService])
trait SlaServiceLike {
  def getSlaByToken(token: String): Future[Sla]
  def setSla(user: String, rps: Int): Future[Sla]
}

class MockSlaService extends SlaServiceLike {

  override def getSlaByToken(token: String): Future[Sla] = Future.successful(Sla("user", 3))

  override def setSla(user: String, rps: Int): Future[Sla] = Future.successful(Sla("user", rps))
}
