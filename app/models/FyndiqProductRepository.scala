  package models

import javax.inject.Inject

import anorm.SqlParser._
import anorm._
import play.api.db.DBApi

import scala.concurrent.Future

case class FyndiqProduct(id: Option[Long] = None,
                         fyndiqId: Long,
                         description: String,
                         isBlockedByFyndiq: Option[Boolean],
                         itemNo: String,
                         fyndiqMomsPercent: Int = 25,
                         numInStock: Int,
                         fyndiqOldprice: String,
                         fyndiqPrice: String,
                         fyndiqResourceUri: String,
                         fyndiqState: String,
                         title: String,
                         fyndiqUrl: String)

/**
  * Helper for pagination.
  */
//case class Page[A](items: Seq[A], page: Int, offset: Long, total: Long) {
//  lazy val prev = Option(page - 1).filter(_ >= 0)
//  lazy val next = Option(page + 1).filter(_ => (offset + items.size) < total)
//}


@javax.inject.Singleton
class FyndiqProductRepository @Inject()(dbapi: DBApi, imageRepository: ImageRepository)(implicit ec: DatabaseExecutionContext) {

  private val db = dbapi.database("wearbal")

  // -- Parsers

  /**
    * Parse a Product from a ResultSet
    */
  private val simple = {
    get[Option[Long]]("fyndiq_product.id") ~
      get[Long]("fyndiq_product.fyndiq_id") ~
      get[String]("fyndiq_product.description") ~
      get[Option[Boolean]]("fyndiq_product.is_blocked_by_fyndiq") ~
      get[String]("fyndiq_product.item_no") ~
      get[Int]("fyndiq_product.moms_percent") ~
      get[Int]("fyndiq_product.num_in_stock") ~
      get[String]("fyndiq_product.oldprice") ~
      get[String]("fyndiq_product.price") ~
      get[String]("fyndiq_product.resource_uri") ~
      get[String]("fyndiq_product.state") ~
      get[String]("fyndiq_product.title") ~
      get[String]("fyndiq_product.url") map {
      case id ~ fyndiqId ~ description ~ isBlockedByFyndiq ~ itemNo ~ momsPercent ~ numInStock ~ oldprice ~ price ~ resourceUri ~ state ~ title ~ url =>
    FyndiqProduct(id, fyndiqId, description, isBlockedByFyndiq, itemNo, momsPercent, numInStock, oldprice, price, resourceUri, state, title, url)
    }
  }


  /**
    * Retrieve a computer from the id.
    */
  def findById(id: Long): Future[Option[FyndiqProduct]] = Future {
    db.withConnection { implicit connection =>
      SQL(
        """
           select * from fyndiq_product fp
           left join product p on fp.product_id = p.id
           left join image on product.id = image.product_id
           where product.id = {id}
        """.stripMargin).on('id -> id).as(simple.singleOpt)
    }
  }(ec)


  /**
    * Update a product.
    *
    * @param product The product values.
    */
  def update(product: FyndiqProduct) = Future {
    db.withConnection { implicit connection =>
      SQL(
        """
          update product
            set fyndiq_id = {fyndiq_id},
            description = {description},
            is_blocked_by_fyndiq = {is_blocked_by_fyndiq},
            item_no = {item_no},
            moms_percent = {moms_percent},
            num_in_stock = {num_in_stock},
            oldprice = {oldprice},
            price = {price},
            resource_uri = {resource_uri},
            state = {state},
            title = {title},
            url = {url}
          )
        """
      ).on(
        'fyndiq_id -> product.fyndiqId,
        'description -> product.description,
        'is_blocked_by_fyndiq -> product.isBlockedByFyndiq,
        'item_no -> product.itemNo,
        'moms_percent -> product.fyndiqMomsPercent,
        'num_in_stock -> product.numInStock,
        'oldprice -> product.fyndiqOldprice,
        'price -> product.fyndiqPrice,
        'resource_uri -> product.fyndiqResourceUri,
        'state -> product.fyndiqState,
        'title -> product.title,
        'url -> product.fyndiqUrl
      ).executeUpdate()
    }
  }(ec)

  /**
    * Insert a new product.
    *
    * @param product The product values.
    */
  def insert(product: FyndiqProduct, images: List[Image]) = Future {
    db.withConnection { implicit connection =>
      SQL(
        """
          insert into product values (
            (select next value for product_seq),
            {fyndiq_id}, {description}, {is_blocked_by_fyndiq}, {item_no}, {moms_percent}, {num_in_stock}, {oldprice}, {price}, {resource_uri}, {state}, {title}, {url}
          )
        """
      ).on(
        'fyndiq_id -> product.fyndiqId,
        'description -> product.description,
        'is_blocked_by_fyndiq -> product.isBlockedByFyndiq,
        'item_no -> product.itemNo,
        'moms_percent -> product.fyndiqMomsPercent,
        'num_in_stock -> product.numInStock,
        'oldprice -> product.fyndiqOldprice,
        'price -> product.fyndiqPrice,
        'resource_uri -> product.fyndiqResourceUri,
        'state -> product.fyndiqState,
        'title -> product.title,
        'url -> product.fyndiqUrl
      ).executeUpdate()
    }
  }(ec)

}
