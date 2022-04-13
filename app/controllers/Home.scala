/**
 *
 * to do sample project
 *
 */

package controllers

import javax.inject._
import play.api.mvc._

import model.ViewValueHome
import lib.persistence.default.UserRepository
import lib.model.User
import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class HomeController @Inject()(val controllerComponents: ControllerComponents) extends BaseController {

  def index() = Action async{ implicit req =>
    val vv = ViewValueHome(
      title  = "Home",
      cssSrc = Seq("main.css"),
      jsSrc  = Seq("main.js")
    )

    val uId = User.Id(1L)

    for{
      userOpt <- UserRepository.get(uId)
    } yield  {
      Ok(views.html.Home(userOpt.get, vv))
    }
  }
}
