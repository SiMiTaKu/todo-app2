package controllers


import lib.model.Todo

import javax.inject._
import play.api.mvc._
import model.ViewValueHome
import lib.persistence.default.TodoRepository
import play.api.data.Forms.{mapping, nonEmptyText, number}
import play.api.data.Form
import play.api.i18n.I18nSupport
import lib.model.Todo.Id

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

case class TodoFormData(category: Int, title: String, body: String)

@Singleton
class TodoController @Inject()(
    val controllerComponents: ControllerComponents
  ) extends BaseController with I18nSupport {

  val error_vv = ViewValueHome(
    title = "Error Page Not Found 404",
    cssSrc = Seq("main.css"),
    jsSrc = Seq("main.js")
  )

  def list() = Action async{ implicit req =>

    val vv = ViewValueHome(
      title = "Todo List",
      cssSrc = Seq("main.css"),
      jsSrc = Seq("main.js")
    )
    for{
      results <- TodoRepository.getAll()
    } yield{
      Ok(views.html.todo.list(results, vv))
    }
  }

  def detail(id: Long) = Action async { implicit request: Request[AnyContent] =>
    val vv = ViewValueHome(
      title  = "Detail",
      cssSrc = Seq("main.css"),
      jsSrc  = Seq("main.js")
    )

    for {
      todo <- TodoRepository.get(lib.model.Todo.Id(id))
    } yield {
      todo match {
        case Some(result) => Ok(views.html.todo.detail(result, vv))
        case None         => NotFound(views.html.page404(error_vv))
      }
    }
  }



  val form = Form(
    mapping(
      "category" -> number,
      "title"    -> nonEmptyText(maxLength = 140),
      "body" -> nonEmptyText(maxLength = 200)
    )(TodoFormData.apply)(TodoFormData.unapply)
  )

  def register() = Action{ implicit request: Request[AnyContent] =>
    val vv = ViewValueHome(
      title  = "Todo Register Form",
      cssSrc = Seq("main.css"),
      jsSrc  = Seq("main.js")
    )

    Ok(views.html.todo.registerForm(form, vv))
  }

  def add() = Action async { implicit request: Request[AnyContent] =>
    val vv = ViewValueHome(
      title  = "Todo Register Form",
      cssSrc = Seq("main.css"),
      jsSrc  = Seq("main.js")
    )

    form.bindFromRequest().fold(
      (formWithErrors: Form[TodoFormData]) =>{
        Future.successful(BadRequest(views.html.todo.registerForm(formWithErrors, vv)))
      },
      (todoFormData: TodoFormData) =>{
        for {
          _ <- TodoRepository.add(Todo.apply(Some(todoFormData.category), todoFormData.title, todoFormData.body, lib.model.Todo.Status(0)))
        } yield {
          Redirect(routes.TodoController.list())
        }
      }
    )
  }
/*
  def remove() = Action async {implicit request: Request[AnyContent] =>
    val idOpt = request.body.asFormUrlEncoded.get("id").headOption
    for {
      result <- TodoRepository.remove(idOpt.map(_.toInt))
    } yield {
      result match {
        case 0 => NotFound(views.html.error.page404())
        case _ => Redirect(routes.TodoController.list)
      }
    }
  }

 */
}