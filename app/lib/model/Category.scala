package lib.model

import ixias.model._
import java.time.LocalDateTime
import Category._

case class Category(
                 id:              Option[Id],
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

  val colorMap = Map(1 -> "ff9393", 2 -> "ffeb9c", 3 -> "d6f697", 4 -> "bbffde", 5 -> "bbe7ff", 6 -> "bdbbff")

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