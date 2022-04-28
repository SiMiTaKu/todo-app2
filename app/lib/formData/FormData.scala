package lib.formData

import play.api.data.Form
import play.api.data.Forms.{mapping, nonEmptyText}

case class CategoryFormData(title:    String, slug:  String, color: String)
case class TodoFormData    (category: String, title: String, body:  String)
case class TodoEditFormData(category: String, title: String, body:  String, state: String)

package object formData {

  val form = Form(
    mapping(
      "category" -> nonEmptyText,
      "title"    -> nonEmptyText(maxLength = 140),
      "body"     -> nonEmptyText(maxLength = 200)
    )(TodoFormData.apply)(TodoFormData.unapply)
  )

  val editForm = Form(
    mapping(
      "category" -> nonEmptyText,
      "title"    -> nonEmptyText(maxLength = 140),
      "body"     -> nonEmptyText(maxLength = 200),
      "state"    -> nonEmptyText
    )(TodoEditFormData.apply)(TodoEditFormData.unapply)
  )

  val categoryForm = Form(
    mapping(
      "title" -> nonEmptyText(maxLength = 140),
      "slug"  -> nonEmptyText(maxLength = 140),
      "color" -> nonEmptyText
    )(CategoryFormData.apply)(CategoryFormData.unapply)
  )
}