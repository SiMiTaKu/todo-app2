package lib.model

import ixias.model._
import java.time.LocalDateTime

// ユーザーを表すモデル
//~~~~~~~~~~~~~~~~~~~~
import Todo._

case class Todo(
                 id:               Option[Id],
                 category_id:      Option[Int],
                 title:            String,
                 body:             String,
                 state:            Option[Int],
                 updatedAt: LocalDateTime = NOW,
                 createdAt: LocalDateTime = NOW
               ) extends EntityModel[Id]

object Todo {
  val  Id = the[Identity[Id]]
  type Id = Long @@ Todo
  type WithNoId = Entity.WithNoId [Id, Todo]
  type EmbeddedId = Entity.EmbeddedId[Id, Todo]

  def apply( category_id: Option[Int], title: String, body: String, state: Option[Int]): WithNoId = {
    new Entity.WithNoId(
      new Todo(
        id = None,
        category_id = category_id,
        body = body,
        title = title,
        state = state,
      )
    )
  }
}