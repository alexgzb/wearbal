package models

import javax.inject.Inject

import anorm.SqlParser._
import anorm._
import play.api.db.DBApi

import scala.concurrent.Future

case class FyndiqProductTable(id: Option[Long] = None,
                              productId: Long,
                              fyndiqId: Long,
                              title: String,
                              description: String,
                              fyndiqMomsPercent: Int = 25,
                              isBlockedByFyndiq: Boolean,
                              fyndiqState: String,
                              fyndiqPrice: String,
                              fyndiqOldprice: String,
                              fyndiqResourceUri: String,
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
    get[Option[Long]]("id") ~
      get[Long]("fyndiq_product.id") ~
      get[Long]("fyndiq_product.fyndiq_id") ~
      get[String]("fyndiq_product.title") ~
      get[String]("fyndiq_product.description") ~
      get[Int]("fyndiq_product.moms_percent") ~
      get[Boolean]("fyndiq_product.is_blocked_by_fyndiq") ~
      get[String]("fyndiq_product.state") ~
      get[String]("fyndiq_product.price") ~
      get[String]("fyndiq_product.oldprice") ~
      get[String]("fyndiq_product.resource_uri") ~
      get[String]("fyndiq_product.url") map {
      case id ~ productId ~ fyndiqId ~ title ~ description ~ momsPercent ~ isBlockedByFyndiq ~ state ~ price ~ oldprice ~ resourceUri ~ url =>
        FyndiqProductTable(id, productId, fyndiqId, title, description, momsPercent, isBlockedByFyndiq, state, price, oldprice, resourceUri, url)
    }
  }


  /**
    * Retrieve a computer from the id.
    */
  def findById(id: Long): Future[Option[FyndiqProductTable]] = Future {
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
    * Update a fyndiq_product.
    *
    * @param product The product values.
    */
  def update(product: FyndiqProductTable) = Future {
    db.withConnection { implicit connection =>
      SQL(
        """
          update fyndiq_product
            set fyndiq_id = {fyndiq_id},
            title = {title},
            description = {description},
            is_blocked_by_fyndiq = {is_blocked_by_fyndiq},
            item_no = {item_no},
            moms_percent = {moms_percent},
            num_in_stock = {num_in_stock},
            oldprice = {oldprice},
            price = {price},
            resource_uri = {resource_uri},
            state = {state},
            url = {url}
          )
        """
      ).on(
        'fyndiq_id -> product.fyndiqId,
        'description -> product.description,
        'is_blocked_by_fyndiq -> product.isBlockedByFyndiq,
        'moms_percent -> product.fyndiqMomsPercent,
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
    * Insert a new fyndiq_product.
    *
    * @param product The product values.
    */
  def insert(product: FyndiqProductTable): Future[Long] = Future {
    db.withConnection { implicit connection =>
      SQL(
        """
          insert into fyndiq_product values (
            (select next value for fyndiq_product_seq),
            {productId},
            {fyndiq_id},
            {title},
            {description},
            {moms_percent},
            {is_blocked_by_fyndiq},
            {state},
            {price},
            {oldprice},
            {url},
            {resource_uri}
          )
        """
      ).on(
        'productId -> product.productId,
        'fyndiq_id -> product.fyndiqId,
        'title -> product.title,
        'description -> product.description,
        'moms_percent -> product.fyndiqMomsPercent,
        'is_blocked_by_fyndiq -> product.isBlockedByFyndiq,
        'state -> product.fyndiqState,
        'price -> product.fyndiqPrice,
        'oldprice -> product.fyndiqOldprice,
        'url -> product.fyndiqUrl,
        'resource_uri -> product.fyndiqResourceUri
      ).executeInsert(scalar[Long].single)
    }
  }(ec)
}
