package lib.model

import ixias.model._

import java.time.LocalDateTime
import Category._
import ixias.util.EnumStatus

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

  sealed abstract class ColorMap(val code: Short, val name: String) extends EnumStatus
  object ColorMap extends EnumStatus.Of[ColorMap] {
    case object LIGHTSALMON extends ColorMap(code = 0, name = "LightSalmon")
    case object LIGHTYELLOW extends ColorMap(code = 1, name = "LightYellow")
    case object AQUAMARINE  extends ColorMap(code = 2, name = "Aquamarine")
    case object SKYBLUE     extends ColorMap(code = 3, name = "SkyBlue")
    case object LIGHTPINK   extends ColorMap(code = 4, name = "LightPink")
    case object VIOLET      extends ColorMap(code = 5, name = "Violet")
  }

  //val colorMap = Map(1 ->"LightSalmon", 2 -> "LightYellow", 3 -> "Aquamarine", 4 -> "SkyBlue", 5 -> "LightPink", 6 -> "Violet")

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