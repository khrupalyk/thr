package services

import models.Sla

import scala.concurrent.Future

trait SlaServiceLike {
  def getSlaByToken(token: String): Future[Sla]
}
