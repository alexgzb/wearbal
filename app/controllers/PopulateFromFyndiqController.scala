package controllers

import javax.inject.{Inject, Singleton}

import com.netaporter.uri.Uri
import internal.{FyndiqProduct, FyndiqProductsResponse}
import models._
import play.api.libs.ws.WSClient
import play.api.mvc._

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}


@Singleton
class PopulateFromFyndiqController @Inject()(val wSClient: WSClient,
                                             val controllerComponents: ControllerComponents,
                                             val productRepository: ProductRepository,
                                             val fyndiqProductRepository: FyndiqProductRepository,
                                             val imageRepository: ImageRepository)
                                            (implicit executionContext: ExecutionContext) extends BaseController {

  def getProducts: Action[AnyContent] = Action.async {
    implicit val baseUrl: Uri = Uri.parse("https://fyndiq.se")
    val url = Uri.parse("/api/v1/product/").addParams(Seq(
      "token" -> "pihLrGOrnQRlEG2ascwElkuOA480v86q",
      "user" -> "niclas.jansson@oddhabit.com",
      "limit" -> "2000"
    ))

    def loop(uri: Option[String], products: List[FyndiqProduct] = List.empty, acc: List[FyndiqProduct] = List.empty): Unit = {
      val newAcc = if (products.nonEmpty) acc.++(products) else acc
      uri match {
        case Some(str) =>
          wSClient.url(baseUrl + str).get().map(_.json.as[FyndiqProductsResponse]).map { response =>
            println(s"meta of response = ${response.meta}")
            loop(response.meta.next, response.objects, newAcc)
          }
        case None =>
          println(s"Storing values")
          createAndStoreProducts(newAcc)
      }
    }

    loop(Some(url.toString))
    productRepository.count().map(p => Ok(s"DB SIZE = $p"))
  }

  private def createAndStoreProducts(fyndiqProducts: List[FyndiqProduct])(implicit baseUrl: Uri): Unit = {
    fyndiqProducts.foreach { fp =>
      fp.variation_group match {
        case Some(vg) =>
          vg.head.variations.foreach {
            variation =>
              val prod = Product(name = fp.title, description = fp.description, sku = variation.merchant_item_no, numInStock = variation.num_in_stock, sellingPrice = fp.price.toDouble, rrp = fp.oldprice.toDouble)
              persistProduct(fp, prod)

          }
        case _ =>
          val prod = Product(name = fp.title, description = fp.description, sku = fp.item_no, numInStock = fp.num_in_stock, sellingPrice = fp.price.toDouble, rrp = fp.oldprice.toDouble)
          persistProduct(fp, prod)
      }
    }
  }

  private def persistProduct(fp: FyndiqProduct, product: Product)(implicit baseUrl: Uri): Unit = {
    productRepository.insert(product).onComplete {
      case Success(id) =>
        fyndiqProductRepository.insert(FyndiqProductTable(None,
          id,
          fp.id,
          fp.title,
          fp.description,
          fp.moms_percent,
          fp.is_blocked_by_fyndiq,
          fp.state,
          fp.price,
          fp.oldprice,
          baseUrl + fp.resource_uri,
          fp.url)
        ).onComplete {
            case Failure(e) => println(e.toString)
            case _ =>
        }
        fp.images.foreach { image =>
          imageRepository.insert(Image(productId = id, url = image)).onComplete {
            case Failure(e) => print(e)
            case _ =>
          }
        }
      case Failure(e) => println(e.toString)
    }
  }

}
