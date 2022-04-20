package controllers

import lib.model.{Category, Todo}
import lib.persistence.default.CategoryRepository
import lib.persistence.db.CategoryFormData

import javax.inject._
import play.api.mvc.{BaseController, _}
import model.{ViewValueHome,ViewValueList,ViewValueRegister,ViewValueDetail,ViewValueEdit}
import play.api.data.{Form, Forms}
import play.api.data.Forms.{mapping, nonEmptyText}
import play.api.i18n.I18nSupport

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class CategoryController @Inject()(
  val controllerComponents: ControllerComponents
  ) extends BaseController with I18nSupport {

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


  val categoryForm = Form(
    mapping(
      "title" -> nonEmptyText(maxLength = 140),
      "slug"  -> nonEmptyText(maxLength = 140),
      "color" -> Forms.text
    )(CategoryFormData.apply)(CategoryFormData.unapply)
  )

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
}