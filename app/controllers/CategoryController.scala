package controllers

import lib.model.{Category, Todo}
import lib.persistence.default.{CategoryRepository, TodoRepository}
import lib.formData.CategoryFormData
import lib.formData.formData.categoryForm

import javax.inject._
import play.api.mvc.{BaseController, _}
import model.{ViewValueEdit, ViewValueError, ViewValueList, ViewValueRegister}
import play.api.data.Form
import play.api.i18n.I18nSupport

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.{Duration, DurationInt}
import scala.concurrent.{Await, Future}


@Singleton
class CategoryController @Inject()(
  val controllerComponents: ControllerComponents
  ) extends BaseController with I18nSupport {

  val error_vv = ViewValueError(
    title  = "Error Page Not Found 404",
    cssSrc = Seq("main.css"),
    jsSrc  = Seq("main.js")
  )

  def list() = Action async{ implicit req =>
    val vv = ViewValueList(
      title  = "Category List",
      cssSrc = Seq("main.css"),
      jsSrc  = Seq("main.js")
    )
    for{
      results <- CategoryRepository.getAll()
    } yield{
      Ok(views.html.category.list(results, vv))
    }
  }

  def register() = Action async{ implicit request: Request[AnyContent] =>
    val vv = ViewValueRegister(
      title  = "Todo Register Form",
      cssSrc = Seq("main.css"),
      jsSrc  = Seq("main.js")
    )
    for{
      categories <- CategoryRepository.getAll()
    } yield {
      Ok(views.html.category.register(categoryForm, categories, vv))
    }
  }

  def add() = Action async { implicit request: Request[AnyContent] =>
    val vv = ViewValueRegister(
      title  = "Todo Register Form",
      cssSrc = Seq("main.css"),
      jsSrc  = Seq("main.js")
    )

    categoryForm.bindFromRequest().fold(
      (formWithErrors: Form[CategoryFormData]) =>{
        for {
          categories <- CategoryRepository.getAll()
        } yield {
          BadRequest(views.html.category.register(formWithErrors, categories, vv))
        }
      },
      (categoryFormData: CategoryFormData) =>{
        for {
          _ <- CategoryRepository.add(Category.apply(categoryFormData.title, categoryFormData.slug, categoryFormData.color.toInt))
        } yield {
          Redirect(routes.CategoryController.list())
        }
      }
    )
  }

  def edit(id: Long) = Action async {implicit request: Request[AnyContent] =>
    val vv = ViewValueEdit(
      title  = s"Edit Category ${id}",
      cssSrc = Seq("main.css"),
      jsSrc  = Seq("main.js")
    )
    for {
      category <- CategoryRepository.get(Category.Id(id))
    } yield {
      category match {
        case Some(result) =>
          Ok(views.html.category.edit(
            id,
            categoryForm.fill(CategoryFormData(
              result.v.name,
              result.v.slug,
              result.v.color.toString
            )),
            vv
          ))
        case None => NotFound(views.html.page404(error_vv))
      }
    }

  }

  def update(id: Long) = Action async { implicit request: Request[AnyContent] =>
    val vv = ViewValueEdit(
      title  = s"Edit Todo ${id}",
      cssSrc = Seq("main.css"),
      jsSrc  = Seq("main.js")
    )
    categoryForm.bindFromRequest().fold(
      (formWithErrors: Form[CategoryFormData]) => {
        Future.successful(BadRequest(views.html.category.edit(id, formWithErrors, vv)))
      },
      (data: CategoryFormData) => {
        for{
          old <- CategoryRepository.get(Category.Id(id))
        } yield {
          old match {
            case None => NotFound(views.html.page404(error_vv))
            case _    => CategoryRepository.update(
              Category(
                id    = old.get.v.id,
                name  = data.title,
                slug  = data.slug,
                color = data.color.toInt,
              ).toEmbeddedId
            )
              Redirect(routes.CategoryController.list)
          }
        }
      }
    )
  }



  def remove() = Action{ implicit request: Request[AnyContent] =>
    val id = request.body.asFormUrlEncoded.get("id").headOption.get.toLong
    val deleteTodo = for{
      todos <- TodoRepository.getAll()
    } yield {
      for(todo <- todos.filter(todo => todo.v.category_id.get.toLong == id)){
        TodoRepository.remove(Todo.Id(todo.v.id.get.toLong))
      }
    }

    val deleteCategory = for {
      categoryRemove <- CategoryRepository.remove(Category.Id(id))
    } yield {
      categoryRemove match {
        case None    => NotFound(views.html.page404(error_vv))
        case Some(_) =>
      }
    }
    Await.ready(deleteTodo, Duration.Inf)
    Await.ready(deleteCategory, Duration.Inf)
    Redirect(routes.CategoryController.list)
  }
}