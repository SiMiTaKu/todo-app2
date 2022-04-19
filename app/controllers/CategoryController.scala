package controllers


import lib.persistence.default.CategoryRepository

import javax.inject._
import play.api.mvc.{BaseController, _}
import model.ViewValueHome
import play.api.i18n.I18nSupport
import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class CategoryController @Inject()(
  val controllerComponents: ControllerComponents
  ) extends BaseController with I18nSupport {

  def list() = Action async{ implicit req =>
    val vv = ViewValueHome(
      title = "Category List",
      cssSrc = Seq("main.css"),
      jsSrc = Seq("main.js")
    )
    for{
      results <- CategoryRepository.getAll()
    } yield{
      Ok(views.html.category.list(results, vv))
    }
  }
}