package models

import javax.inject.Inject

import anorm.SqlParser._
import anorm._
import play.api.db.DBApi

import scala.concurrent.Future

case class Image(id: Option[Long] = None, productId: Long, url: String)

@javax.inject.Singleton
class ImageRepository @Inject()(dbapi: DBApi)(implicit ec: DatabaseExecutionContext) {

  private val db = dbapi.database("wearbal")

  /**
    * Parse a Image from a ResultSet
    */
  private[models] val simple = {
    get[Option[Long]]("image.id") ~
      get[Long]("image.product_id") ~
      get[String]("image.url") map {
      case id ~ productId ~ url => Image(id, productId, url)
    }
  }

  def deleteForProduct(productId: Int) = Future {
    db.withConnection { implicit connection =>
      SQL("delete from image where product_id = {product_id}").on('product_id -> productId).executeUpdate()
    }
  }(ec)

  def delete(id: Long) = Future {
    db.withConnection { implicit connection =>
      SQL("delete from image where id = {id}").on('id -> id).executeUpdate()
    }
  }(ec)

  def insert(image: Image) = Future {
    db.withConnection { implicit connection =>
      SQL(
        """
           insert into image values (
            (select next value for image_seq), {product_id}, {url}
           )
        """.stripMargin).on('product_id -> image.productId, 'url -> image.url)
    }
  }

  /**
    * Construct the Map[String,String] needed to fill a select options set.
    */
  def options: Future[Seq[(String, String)]] = Future(db.withConnection { implicit connection =>
    SQL("select * from image order by id").as(simple *).
      foldLeft[Seq[(String, String)]](Nil) { (cs, c) =>
      cs :+ (c.productId.toString -> c.url)
    }
  })(ec)

  def findById(id: Long): Future[List[Image]] = Future {
    db.withConnection { implicit connection =>
      SQL("select * from image where id  = {id}").on('id -> id).as(simple.*)
    }
  }(ec)


}


