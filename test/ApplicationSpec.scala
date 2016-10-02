import org.joda.time.DateTime
import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._

import play.api.test._
import play.api.test.Helpers._
import services.RpsService

@RunWith(classOf[JUnitRunner])
class ApplicationSpec extends Specification {

  val rps = new RpsService

  "Application" should {

    "render the index page" in {
      val token = "string"
//      print((1 to 100).size)
      1 to 100 foreach {
        _ => rps.tick(token)
      }

      Thread.sleep(500)
//      val now = DateTime.now()

      println(rps.get(token))
      Thread.sleep(600)
      println(rps.get(token))
      ok
    }
  }
}
