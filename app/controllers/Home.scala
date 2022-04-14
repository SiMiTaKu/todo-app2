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
import lib.persistence.default.TodoRepository
import lib.model.{Todo, User}
import lib.model.User.Status.IS_ACTIVE

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class HomeController @Inject()(val controllerComponents: ControllerComponents) extends BaseController {

  def index() = Action{ implicit req =>
    val vv = ViewValueHome(
      title  = "Home",
      cssSrc = Seq("main.css"),
      jsSrc  = Seq("main.js")
    )

    Ok(views.html.Home(vv))

    /*
    新規ユーザー登録
    UserRepository.add(User.apply("KANTA", 23.toShort, IS_ACTIVE ))
     */

    /*
    ユーザー情報表示試し
    val uId = User.Id(1L)
    for{
      userOpt <- UserRepository.get(uId)
    } yield  {
      Ok(views.html.Home(userOpt.get, vv))
    }

     */
  }
}
