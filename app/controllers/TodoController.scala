package controllers


import lib.model.Todo
import javax.inject._
import play.api.mvc._
import model.ViewValueHome

import lib.persistence.default.TodoRepository
import scala.concurrent.ExecutionContext.Implicits.global


@Singleton
class TodoController @Inject()(val controllerComponents: ControllerComponents) extends BaseController {
  def list() = Action async{ implicit req =>

    val vv = ViewValueHome(
      title = "TodoList",
      cssSrc = Seq("main.css"),
      jsSrc = Seq("main.js")
    )

    val todoId = Todo.Id(1L)
    for{
      todo <- TodoRepository.get(todoId)
    } yield {
      Ok(views.html.todo.list(todo.get, vv))
    }
  }
}