package models

import anorm.SqlParser.{get, scalar}
import anorm.{SQL, ~}
import com.google.inject.{Inject, Singleton}
import play.api.db.DBApi

import scala.concurrent.Future

case class Product(id: Option[Long] = None,
                   name: String,
                   description: String,
                   sku: String,
                   ean: Option[String] = None,
                   numInStock: Int,
                   purchasePrice: Option[Double] = None,
                   sellingPrice: Double,
                   rrp: Double,
                   brand: Option[String] = None,
                   companyId: Option[Long] = None)

/**
  * Helper for pagination.
  */
case class Page[A](items: Seq[A], page: Int, offset: Long, total: Long) {
  lazy val prev = Option(page - 1).filter(_ >= 0)
  lazy val next = Option(page + 1).filter(_ => (offset + items.size) < total)
}

@Singleton
class ProductRepository @Inject()(dBApi: DBApi, fyndiqProductRepository: FyndiqProductRepository, imageRepository: ImageRepository) (ec: DatabaseExecutionContext) {

  private val db = dBApi.database("wearbal")

  /**
    * Parse a Product from a ResultSet
    */
  private val simple = {
      get[Option[Long]]("product.id") ~
        get[String]("product.name") ~
        get[String]("product.description") ~
        get[String]("product.sku") ~
        get[Option[String]]("product.ean") ~
        get[Int]("product.num_in_stock") ~
        get[Option[Double]]("product.purchase_price") ~
        get[Double]("product.selling_price") ~
        get[Double]("product.rrp") ~
        get[Option[String]]("product.brand") ~
        get[Option[Long]]("product.company_id") map {
        case id ~ name ~ description ~ sku ~ ean ~ numInStock ~ purchasePrice ~ sellingPrice ~ rrp ~ brand ~ companyId =>
        Product(id, name, description, sku, ean, numInStock, purchasePrice, sellingPrice, rrp, brand, companyId)
    }
  }

  /**
    * Parse a (Product, Image) from a ResultSet
    */
  private val withImage = simple ~ (imageRepository.simple ?) map {
    case product ~ image => (product, image)
  }

  // -- Queries

  /**
    * Retrieve a product from the id.
    */
  def findById(id: Long): Future[Option[Product]] = Future {
    db.withConnection { implicit connection =>
      SQL(
        """
           select * from product join images i on p.id = i.product_id
           left join image on product.id = image.product_id
           left join company on product.company_id = company.id
           left join fyndiq_product on product.id = fyndiq_product.product_id
           where product.id = {id}
        """.stripMargin).on('id -> id).as(simple.singleOpt)
    }
  }(ec)

  /**
    * Return a page of (Product,Images).
    *
    * @param page     Page to display
    * @param pageSize Number of products per page
    * @param orderBy  Product property used for sorting
    * @param filter   Filter applied on the title column
    */
  def list(page: Int = 0,
           pageSize: Int = 10,
           orderBy: Int = 1,
           filter: String = "%"): Future[Page[Product]] = Future {

    val offest = pageSize * page

    db.withConnection { implicit connection =>

      val products = SQL(
        """
           select * from product p
          left join image i on p.id = i.product_id
          left join fyndiq_product fp on p.id = fp.product_id
          left join company c on c.id = p.company_id
          where p.name like {filter}
          order by {orderBy} nulls last
          limit {pageSize} offset {offset}
        """
      ).on(
        'pageSize -> pageSize,
        'offset -> offest,
        'filter -> filter,
        'orderBy -> orderBy
      ).as(simple.*)

      val totalRows = SQL(
        """
          select count(*) from product p
          where p.name like {filter}
        """
      ).on(
        'filter -> filter
      ).as(scalar[Long].single)

      Page(products, page, offest, totalRows)

    }

  }(ec)

  /**
    * Insert a new product.
    *
    * @param product The product values.
    */
  def update(product: Product): Future[Int] = Future {
    db.withConnection { implicit connection =>
      SQL(
        """
          update product
            set name = {name},
            description = {description},
            sku = {sku},
            ean = {ean},
            num_in_stock = {num_in_stock},
            purchase_price = {purchase_price},
            selling_price = {selling_price},
            rrp = {rrp},
            brand = {brand},
            company_id = {company_id}
          )
        """
      ).on(
        'name -> product.name,
        'description -> product.description,
        'sku -> product.sku,
        'ean -> product.ean,
        'num_in_stock -> product.numInStock,
        'purchase_price -> product.purchasePrice,
        'selling_price -> product.sellingPrice,
        'rrp -> product.rrp,
        'brand -> product.brand,
        'company_id -> product.companyId
      ).executeUpdate()
    }
  }(ec)


  /**
    * Insert a new product.
    *
    * @param product The product values.
    */
  def insert(product: Product): Future[Long] = Future {
    db.withConnection { implicit connection =>
      SQL(
        """
          insert into product values (
            (select next value for product_seq),
            {name},
            {description},
            {sku},
            {ean},
            {num_in_stock},
            {purchase_price},
            {selling_price},
            {rrp},
            {brand},
            {company_id}
          )
        """
      ).on(
        'name -> product.name,
        'description -> product.description,
        'sku -> product.sku,
        'ean -> product.ean,
        'num_in_stock -> product.numInStock,
        'purchase_price -> product.purchasePrice,
        'selling_price -> product.sellingPrice,
        'rrp -> product.rrp,
        'brand -> product.brand,
        'company_id -> product.companyId
      ).executeInsert(scalar[Long].single)
    }
  }(ec)

  /**
    * Delete a product and the images belonging to the product
    *
    * @param id Id of the product to delete.
    */
  def delete(id: Long): Future[Int] = Future {
    db.withConnection { implicit connection =>
      SQL("delete from product where id = {id}").on('id -> id).executeUpdate()
      SQL("delete from image where product_id = {id}").on('id -> id).executeUpdate()
    }
  }(ec)


  def count(): Future[Int] = Future {
    db.withConnection { implicit  connection =>
      SQL("select count(*) from product").as(scalar[Int].single)
    }
  }(ec)

}