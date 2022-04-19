package lib.model

import ixias.model._
import java.time.LocalDateTime
import Category._

case class Category(
                 id:     Option[Id],
                 name:            String,
                 slug:            String,
                 color:           Int,
                 updatedAt: LocalDateTime = NOW,
                 createdAt: LocalDateTime = NOW
               ) extends EntityModel[Id]

object Category {
  val  Id = the[Identity[Id]]
  type Id = Long @@ Category
  type WithNoId = Entity.WithNoId [Id, Category]
  type EmbeddedId = Entity.EmbeddedId[Id, Category]

  def apply(name: String, slug: String, color: Int): WithNoId = {
    new Entity.WithNoId(
      new Category(
        id    = None,
        name  = name,
        slug  = slug,
        color = color
      )
    )
  }
}