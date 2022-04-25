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
    CategoryRepository.getAll().map{ categories => Ok(views.html.category.list(categories, vv))}
  }

  def register() = Action async{ implicit request: Request[AnyContent] =>
    val vv = ViewValueRegister(
      title  = "Todo Register Form",
      cssSrc = Seq("main.css"),
      jsSrc  = Seq("main.js")
    )
    CategoryRepository.getAll().map{ categories => Ok(views.html.category.register(categoryForm, categories, vv))}
  }

  def add() = Action async { implicit request: Request[AnyContent] =>
    val vv = ViewValueRegister(
      title  = "Todo Register Form",
      cssSrc = Seq("main.css"),
      jsSrc  = Seq("main.js")
    )

    categoryForm.bindFromRequest().fold(
      (formWithErrors: Form[CategoryFormData]) =>{
        CategoryRepository.getAll().map(categories => BadRequest(views.html.category.register(formWithErrors, categories, vv)))
      },
      (categoryFormData: CategoryFormData) =>{
        CategoryRepository.add(Category.apply(categoryFormData.title, categoryFormData.slug, Category.ColorMap(categoryFormData.color.toShort))).map{_ =>
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
    CategoryRepository.get(Category.Id(id)).map{
      case None => NotFound(views.html.page404(error_vv))
      case Some(result) => Ok(views.html.category.edit(
        id,
        categoryForm.fill(CategoryFormData(
          result.v.name,
          result.v.slug,
          result.v.color.toString
        )),
        vv
      ))
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
        CategoryRepository.get(Category.Id(id)).map{
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
    )
  }



  def remove(id: Long) = Action async { implicit request: Request[AnyContent] =>
    TodoRepository.getAll().map{
      case Nil     => NotFound(views.html.page404(error_vv))
      case todos   => for(todo <- todos.filter(todo => todo.v.category_id.toLong == id)){
                        todo.v.id.map(todoId => TodoRepository.remove(todoId))
                      }
    }

    CategoryRepository.remove(Category.Id(id)).map{
      case None                 => NotFound(views.html.page404(error_vv))
      case Some(categoryRemove) => Redirect(routes.CategoryController.list)
    }
  }
}