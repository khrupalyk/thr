package services

import java.util.concurrent.{ConcurrentHashMap, ConcurrentMap}

import com.google.inject.ImplementedBy
import models.Sla

@ImplementedBy(classOf[SlaCache])
trait SlaCacheLike {

  def put(token: String, value: Sla)

  def get(token: String): Option[Sla]
}

class SlaCache extends SlaCacheLike {

  private val map = new ConcurrentHashMap[String, Sla]

  def put(token: String, value: Sla) = {
    map.put(token, value)
  }

  def get(token: String): Option[Sla] = Option(map.get(token))
}
