package controllers

import com.google.inject.Inject
import helpers.SessionAwareAction
import play.api._
import play.api.mvc._
import scheduler.{MessageProducer, Scheduler}

import scala.concurrent.Future

class Application @Inject()(sessionAwareAction: SessionAwareAction) extends Controller {

  var i = 0
  def index = sessionAwareAction.async { r =>
    Future.successful(Ok(views.html.index("Your new application is ready.")))
  }

}