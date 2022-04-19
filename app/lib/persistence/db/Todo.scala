package lib.persistence.db

import java.time.LocalDateTime
import slick.jdbc.JdbcProfile
import ixias.persistence.model.Table
import lib.model.Todo

case class TodoTable[P <: JdbcProfile]()(implicit val driver: P)
  extends Table[Todo, P] {
  import api._

  lazy val dsn = Map(
    "master" -> DataSourceName("ixias.db.mysql://master/to_do"),
    "slave"  -> DataSourceName("ixias.db.mysql://slave/to_do")
  )

  class Query extends BasicQuery(new Table(_)) {}
  lazy val query = new Query

  class Table(tag: Tag) extends BasicTable(tag, "to_do") {
    import Todo._
    def id          = column[Id]            ("id",          O.UInt64, O.PrimaryKey, O.AutoInc)
    def category_id = column[Int]           ("category_id", O.UInt64)
    def title       = column[String]        ("title",       O.Utf8Char255)
    def body        = column[String]        ("body",        O.Utf8Char255)
    def state       = column[Status]        ("state",       O.UInt8)
    def updatedAt   = column[LocalDateTime] ("updated_at",  O.TsCurrent)
    def createdAt   = column[LocalDateTime] ("created_at",  O.Ts)

    type TableElementTuple = (
      Option[Id], Option[Int], String, String, Status, LocalDateTime, LocalDateTime
      )

    // DB <=> Scala の相互のmapping定義
    def * = (id.?, category_id.?, title, body, state, updatedAt, createdAt) <> (
      // Tuple(table) => Model
      (t: TableElementTuple) => Todo(
        t._1, t._2, t._3, t._4, t._5, t._6, t._7,
      ),
      // Model => Tuple(table)
      (v: TableElementType) => Todo.unapply(v).map { t => (
        t._1, t._2, t._3, t._4, t._5, LocalDateTime.now(), t._7
      )}
    )
  }
}
