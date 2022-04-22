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
import scala.concurrent.Future


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
          _ <- CategoryRepository.add(Category.apply(categoryFormData.title, categoryFormData.slug, Category.ColorMap(categoryFormData.color.toShort)))
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
            case None      => NotFound(views.html.page404(error_vv))
            case Some(old) => CategoryRepository.update(
              Category(
                id    = old.v.id,
                name  = data.title,
                slug  = data.slug,
                color = Category.ColorMap(data.color.toShort),
              ).toEmbeddedId
            )
              Redirect(routes.CategoryController.list)
          }
        }
      }
    )
  }



  def remove(id: Long) = Action async { implicit request: Request[AnyContent] =>

    for{
      todos <- TodoRepository.getAll()
    } yield {
      for(todo <- todos.filter(todo => todo.v.category_id.toLong == id)){
          todo.v.id.map(todoId => TodoRepository.remove(todoId))
      }
    }

    for {
      categoryRemove <- CategoryRepository.remove(Category.Id(id))
    } yield {
      categoryRemove match {
        case None                 => NotFound(views.html.page404(error_vv))
        case Some(categoryRemove) => Redirect(routes.CategoryController.list)
      }
    }
  }
}