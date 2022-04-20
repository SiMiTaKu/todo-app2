package controllers

import lib.model.{Todo,Category}
import lib.persistence.db.{TodoEditFormData, TodoFormData}

import javax.inject._
import play.api.mvc._
import model.ViewValueHome
import lib.persistence.default.{TodoRepository,CategoryRepository}
import play.api.data.Forms.{mapping, nonEmptyText}
import play.api.data.Form
import play.api.i18n.I18nSupport
import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class TodoController @Inject()(
    val controllerComponents: ControllerComponents
  ) extends BaseController with I18nSupport {

  val error_vv = ViewValueHome(
    title  = "Error Page Not Found 404",
    cssSrc = Seq("main.css"),
    jsSrc  = Seq("main.js")
  )

  def list() = Action async{ implicit req =>
    val vv  = ViewValueHome(
      title  = "Todo List",
      cssSrc = Seq("main.css"),
      jsSrc  = Seq("main.js")
    )
    for{
      results    <- TodoRepository.getAll()
      categories <- CategoryRepository.getAll()
    } yield{
      Ok(views.html.todo.list(results, categories, vv))
    }
  }

  def detail(id: Long) = Action async { implicit request: Request[AnyContent] =>
    val vv = ViewValueHome(
      title  = s"Detail  Todo No.${id}",
      cssSrc = Seq("main.css"),
      jsSrc  = Seq("main.js")
    )
    for{
      todo       <- TodoRepository.get(lib.model.Todo.Id(id))
      categories <- CategoryRepository.getAll()
    } yield {
      todo match {
        case Some(result) => Ok(views.html.todo.detail(result, categories, vv))
        case None         => NotFound(views.html.page404(error_vv))
      }
    }
  }

  val form = Form(
    mapping(
      "category" -> nonEmptyText,
      "title"    -> nonEmptyText(maxLength = 140),
      "body"     -> nonEmptyText(maxLength = 200)
    )(TodoFormData.apply)(TodoFormData.unapply)
  )

  def register() = Action async{ implicit request: Request[AnyContent] =>
    val vv = ViewValueHome(
      title  = "Todo Register Form",
      cssSrc = Seq("main.css"),
      jsSrc  = Seq("main.js")
    )
    for{
      categories <- CategoryRepository.getAll()
    } yield {
      Ok(views.html.todo.registerForm(form, categories, vv))
    }
  }

  def add() = Action async { implicit request: Request[AnyContent] =>
    val vv = ViewValueHome(
      title  = "Todo Register Form",
      cssSrc = Seq("main.css"),
      jsSrc  = Seq("main.js")
    )

    form.bindFromRequest().fold(
      (formWithErrors: Form[TodoFormData]) =>{
        for {
          categories <- CategoryRepository.getAll()
        } yield {
          BadRequest(views.html.todo.registerForm(formWithErrors, categories, vv))
        }
      },
      (todoFormData: TodoFormData) =>{
        for {
          _ <- TodoRepository.add(Todo.apply(Some(Category.Id(todoFormData.category.toLong)), todoFormData.title, todoFormData.body))
        } yield {
          Redirect(routes.TodoController.list())
        }
      }
    )
  }

  def remove() = Action async {implicit request: Request[AnyContent] =>
    val id = request.body.asFormUrlEncoded.get("id").headOption.get.toLong
    for {
      result <- TodoRepository.remove(lib.model.Todo.Id(id))
    } yield {
      result match {
        case Some(result) => Redirect(routes.TodoController.list)
        case None         => NotFound(views.html.page404(error_vv))
      }
    }
  }

  val editForm = Form(
    mapping(
      "category" -> nonEmptyText,
      "title"    -> nonEmptyText(maxLength = 140),
      "body"     -> nonEmptyText(maxLength = 200),
      "state"    -> nonEmptyText
    )(TodoEditFormData.apply)(TodoEditFormData.unapply)
  )

  def edit(id: Long) = Action async { implicit request: Request[AnyContent] =>
    val vv = ViewValueHome(
      title  = s"Edit Todo ${id}",
      cssSrc = Seq("main.css"),
      jsSrc  = Seq("main.js")
    )
    for{
      todo <- TodoRepository.get(lib.model.Todo.Id(id))
      categories <- CategoryRepository.getAll()
    } yield {
      todo match {
        case Some(result) =>
          Ok(views.html.todo.edit(
            id,
            editForm.fill(TodoEditFormData(
              result.v.category_id.get.toString,
              result.v.title,
              result.v.body,
              result.v.state.toString
            )),
            categories,
            vv
          ))
        case None => NotFound(views.html.page404(error_vv))
      }
    }
  }

  def update(id: Long) = Action async { implicit request: Request[AnyContent] =>
    val vv = ViewValueHome(
      title  = s"Edit Todo ${id}",
      cssSrc = Seq("main.css"),
      jsSrc  = Seq("main.js")
    )

    editForm.bindFromRequest().fold(
      (formWithErrors: Form[TodoEditFormData]) => {
        for{
          categories <- lib.persistence.default.CategoryRepository.getAll()
        }yield{
          BadRequest(views.html.todo.edit(id, formWithErrors, categories, vv))
        }
      },
    (data: TodoEditFormData) => {

      val todoEmbeddedID: Todo#EmbeddedId =
        new Todo(
          id          = Some(lib.model.Todo.Id(id)),
          category_id = Some(Category.Id(data.category.toLong)),
          title       = data.title,
          body        = data.body,
          state       = lib.model.Todo.Status(data.state.toShort)
        ).toEmbeddedId

        for {
          count <- TodoRepository.update(todoEmbeddedID)
        } yield {
          count match {
            case None => NotFound(views.html.page404(error_vv))
            case _    => Redirect(routes.TodoController.list)
          }
        }
      }
    )
  }
}