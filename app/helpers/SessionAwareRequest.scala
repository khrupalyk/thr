package helpers

import java.util.UUID

import com.google.inject.{Singleton, Inject}
import com.typesafe.config.{ConfigFactory, Config}
import play.api.mvc._
import services.{RpsServiceLike, ThrottlingServiceLike, ThrottlingService}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class SessionAwareRequest[A](token: Option[String], request: Request[A]) extends WrappedRequest[A](request)

@Singleton
class SessionAwareAction @Inject()(
  thr: ThrottlingServiceLike,
  rpsService: RpsServiceLike,
  config: Config,
  implicit val ex: ExecutionContext)
  extends ActionBuilder[SessionAwareRequest]
    with Controller {

  val rpsEnabled = true

  def invokeBlock[A](request: Request[A], block: SessionAwareRequest[A] => Future[Result]) = {
    val token = request.headers.get("X-Auth-Token")
    if(rpsEnabled) {
      rpsService.tick(token.getOrElse("unknown"))
      thr.isRequestAllowed(token) flatMap {
        isAllowed =>
          if(isAllowed) {
            block(new SessionAwareRequest[A](token, request))
          } else {
            Future.successful(BadRequest)
          }
      }
    } else block(new SessionAwareRequest[A](token, request))

  }
}
