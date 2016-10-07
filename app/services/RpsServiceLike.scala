package services

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.{LongAdder, AtomicInteger}

import com.google.inject.{Singleton, ImplementedBy}
import org.joda.time.DateTime

@ImplementedBy(classOf[RpsService])
trait RpsServiceLike {
  def tick(token: String): Unit
  def get(token: String): RpsCounter
}

@Singleton
class RpsService extends RpsServiceLike {
  private val map = new ConcurrentHashMap[String, RpsCounter]
  private val newCounter = () => new RpsCounter

  override def tick(token: String): Unit = {
    val now = DateTime.now
    Option(map.get(token)) match {
      case Some(rpsInfo) =>
        if(now.minusSeconds(1).isBefore(rpsInfo.lastDate)) {
          rpsInfo.count.increment()
        } else  rpsInfo.reset(now)
      case None => map.put(token, newCounter())
    }
  }

  override def get(token: String): RpsCounter = {
    val now = DateTime.now
    val emptyRps = newCounter()
    val rsp = Option(map.get(token)).getOrElse(emptyRps)
    if(now.minusSeconds(1).isBefore(rsp.lastDate)) {
      rsp
    } else emptyRps
  }
}

class RpsCounter {
  var lastDate: DateTime = DateTime.now
  val count = new LongAdder
  def reset(now: DateTime) = {
    count.reset()
    lastDate = now
  }
}