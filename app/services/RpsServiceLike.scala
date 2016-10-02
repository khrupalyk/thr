package services

import java.util.concurrent.atomic.{LongAdder, AtomicInteger}

import org.joda.time.DateTime

trait RpsServiceLike {
  def tick(token: String): Unit
  def get(token: String): RpsCounter
}

class RpsService extends RpsServiceLike {
  val map = collection.mutable.HashMap.empty[String, RpsCounter]
  val newCounter = () => RpsCounter(DateTime.now, new LongAdder())

  override def tick(token: String): Unit = {
    val now = DateTime.now
    map.get(token) match {
      case Some(rpsInfo) =>
        if(now.minusSeconds(1).isBefore(rpsInfo.lastDate)) {
          rpsInfo.count.increment()
        } else  map.put(token, newCounter())
      case None => map.put(token, newCounter())
    }
  }

  override def get(token: String): RpsCounter = {
    val now = DateTime.now
    val emptyRps = newCounter()
    val rsp = map.getOrElse(token, emptyRps)
    if(now.minusSeconds(1).isBefore(rsp.lastDate)) {
      rsp
    } else emptyRps
  }
}

case class RpsCounter(lastDate: DateTime, count: LongAdder)