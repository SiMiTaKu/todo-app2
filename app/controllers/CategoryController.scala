package controllers


import javax.inject._
import play.api.mvc._
import model.ViewValueHome

@Singleton
class CategoryController @Inject()(val controllerComponents: ControllerComponents) extends BaseController {
  def list() = Action { implicit req =>
    val vv = ViewValueHome(
      title  = "CategoryList",
      cssSrc = Seq("main.css"),
      jsSrc  = Seq("main.js")
    )

    Ok(views.html.category.list(vv))
  }

}