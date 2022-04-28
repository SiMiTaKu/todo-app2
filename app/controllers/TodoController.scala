package controllers

import lib.model.{Category, Todo}
import lib.formData.{TodoEditFormData, TodoFormData}
import lib.formData.formData.{editForm, form}

import javax.inject._
import play.api.mvc._
import model.{ViewValueDetail, ViewValueEdit, ViewValueError, ViewValueList, ViewValueRegister}
import lib.persistence.default.{CategoryRepository, TodoRepository}
import play.api.data.Form
import play.api.i18n.I18nSupport

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future.successful

@Singleton
class TodoController @Inject()(
    val controllerComponents: ControllerComponents
  ) extends BaseController with I18nSupport {

  val error_vv = ViewValueError(
    title  = "Error Page Not Found 404",
    cssSrc = Seq("main.css"),
    jsSrc  = Seq("main.js")
  )

  def list() = Action async{ implicit req =>
    val vv  = ViewValueList(
      title  = "Todo List",
      cssSrc = Seq("main.css"),
      jsSrc  = Seq("main.js")
    )
    val todos      = TodoRepository.getAll()
    val categories = CategoryRepository.getAll()
    for{
      todoList     <- todos
      categoryList <- categories
    } yield{
      Ok(views.html.todo.list(todoList, categoryList, vv))
    }
  }

  def detail(id: Long) = Action async { implicit request: Request[AnyContent] =>
    val vv = ViewValueDetail(
      title  = s"Detail  Todo No.${id}",
      cssSrc = Seq("main.css"),
      jsSrc  = Seq("main.js")
    )
    val todoGet    = TodoRepository.get(Todo.Id(id))
    val categories = CategoryRepository.getAll()

    for{
      todo         <- todoGet
      categoryList <- categories
    } yield {
      todo match {
        case Some(result) => Ok(views.html.todo.detail(result, categoryList, vv))
        case None         => NotFound(views.html.page404(error_vv))
      }
    }
  }

  def register() = Action async{ implicit request: Request[AnyContent] =>
    val vv = ViewValueRegister(
      title  = "Todo Register Form",
      cssSrc = Seq("main.css"),
      jsSrc  = Seq("main.js")
    )
    CategoryRepository.getAll().map { categories =>
      Ok(views.html.todo.register(form, categories, vv))
    }
  }

  def add() = Action async { implicit request: Request[AnyContent] =>
    val vv = ViewValueRegister(
      title  = "Todo Register Form",
      cssSrc = Seq("main.css"),
      jsSrc  = Seq("main.js")
    )

    form.bindFromRequest().fold(
      (formWithErrors: Form[TodoFormData]) =>{
        CategoryRepository.getAll().map { categories => BadRequest(views.html.todo.register(formWithErrors, categories, vv))
        }
      },
      (todoFormData: TodoFormData) =>{
        TodoRepository.add(Todo.apply(Category.Id(todoFormData.category.toLong), todoFormData.title, todoFormData.body)).map{ _ =>
          Redirect(routes.TodoController.list())
        }
      }
    )
  }

  def remove(id: Long) = Action async {implicit request: Request[AnyContent] =>
    TodoRepository.remove(Todo.Id(id)).map {
      case Some(result) => Redirect(routes.TodoController.list)
      case None         => NotFound(views.html.page404(error_vv))
    }
  }

  def edit(id: Long) = Action async { implicit request: Request[AnyContent] =>
    val vv = ViewValueEdit(
      title  = s"Edit Todo ${id}",
      cssSrc = Seq("main.css"),
      jsSrc  = Seq("main.js")
    )

    val todoGet    = TodoRepository.get(Todo.Id(id))
    val categories = CategoryRepository.getAll()
    for{
      todo         <- todoGet
      categoryList <- categories
    } yield {
      todo match {
        case None         => NotFound(views.html.page404(error_vv))
        case Some(result) => Ok(views.html.todo.edit(
                               id,
                               editForm.fill(TodoEditFormData(
                                 result.v.category_id.toString,
                                 result.v.title,
                                 result.v.body,
                                 result.v.state.toString
                               )),
                               categoryList,
                               vv
                             ))
      }
    }
  }



  def update(id: Long) = Action async { implicit request: Request[AnyContent] =>
    val vv = ViewValueEdit(
      title  = s"Edit Todo ${id}",
      cssSrc = Seq("main.css"),
      jsSrc  = Seq("main.js")
    )

    editForm.bindFromRequest().fold(
      (formWithErrors: Form[TodoEditFormData]) => {
        CategoryRepository.getAll().map{ categories =>
          BadRequest(views.html.todo.edit(id, formWithErrors, categories, vv))
        }
      },
    (data: TodoEditFormData) => {
      for{
        result <- TodoRepository.get(Todo.Id(id))
        _      <- result match {
                    case None      => successful(None)
                    case Some(old) => TodoRepository.update(
                                        old.map(_.copy(
                                          category_id = Category.Id(data.category.toLong),
                                          title       = data.title,
                                          body        = data.body,
                                          state       = Todo.Status(data.state.toShort)
                                        ))
                                      )
                  }
      } yield {
        result match {
          case None => NotFound(views.html.page404(error_vv))
          case _    => Redirect(routes.TodoController.list)
        }
      }
    })
  }
}