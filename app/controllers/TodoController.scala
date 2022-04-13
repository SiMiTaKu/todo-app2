package controllers


import javax.inject._
import play.api.mvc._
import model.ViewValueHome


@Singleton
class TodoController @Inject()(val controllerComponents: ControllerComponents) extends BaseController {

  def list() = Action { implicit req =>

    val vv = ViewValueHome(
      title = "TodoList",
      cssSrc = Seq("main.css"),
      jsSrc = Seq("main.js")
    )

    Ok(views.html.todo.list(vv))

  }
}