package controllers

import javax.inject.{Inject, Singleton}

import com.netaporter.uri.Uri
import internal.{FyndiqProductsResponse, FyndiqProduct}
import models.{Image, ImageRepository, Product, ProductRepository}
import play.api.libs.ws.WSClient
import play.api.mvc._

import scala.concurrent.ExecutionContext


@Singleton
class PopulateFromFyndiqController @Inject()(val wSClient: WSClient,
                                             val controllerComponents: ControllerComponents,
                                             val productRepository: ProductRepository,
                                             val imageRepository: ImageRepository)
                                            (implicit executionContext: ExecutionContext) extends BaseController {

  def getProducts: Action[AnyContent] = Action.async {
    val baseUrl = Uri.parse("https://fyndiq.se")
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
            //println(s"meta of response = ${response.meta}")
            loop(response.meta.next, response.objects, newAcc)
          }
        case None =>
          //println(s"Storing values")
          createAndStoreProducts(newAcc)
      }
    }

    loop(Some(url.toString))
    productRepository.count().map(p => Ok(s"DB SIZE = $p"))
  }

  def createAndStoreProducts(fyndiqProducts: List[FyndiqProduct]) = {
    fyndiqProducts.map { fp =>
      fp.variation_group match  {
        case Some(vg) =>
          vg.head.variations.map {
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

  def persistProduct(fyndiqProduct: FyndiqProduct, product: Product) = {
    productRepository.insert(product).map {id =>
      fyndiqProduct.images.map { image =>
        imageRepository.insert(Image(productId = id, url = image))
      }
    }
  }

}
