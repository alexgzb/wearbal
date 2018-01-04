package controllers

import javax.inject.Inject

import models._
import play.api.data.Forms._
import play.api.data._
import play.api.mvc._
import views._

import scala.concurrent.{ExecutionContext, Future}

/**
  * Manage a database of computers
  */
class HomesController @Inject()(productService: ProductRepository,
                                 fyndiqProductService: FyndiqProductRepository,
                                imageService: ImageRepository,
                                cc: MessagesControllerComponents)(implicit ec: ExecutionContext)
  extends MessagesAbstractController(cc) {

  private val logger = play.api.Logger(this.getClass)

  /**
    * This result directly redirect to the application home.
    */
  val Home = Redirect(routes.HomesController.list(0, 2, ""))

  /**
    * Describe the computer form (used in both edit and create screens).
    */
  val productForm = Form(
    mapping(
      "id" -> ignored(None: Option[Long]),
      "productId" -> longNumber,
      "fyndiq_id" -> longNumber,
      "title" -> nonEmptyText,
      "description" -> nonEmptyText,
      "momsPercent" -> number,
      "isBlockedByFyndiq" -> boolean,
      "state" -> nonEmptyText,
      "price" -> nonEmptyText,
      "oldprice" -> nonEmptyText,
      "resourceUri" -> nonEmptyText,
      "url" -> nonEmptyText
    )(FyndiqProductTable.apply)(FyndiqProductTable.unapply)
  )

  // -- Actions

  /**
    * Handle default path requests, redirect to computers list
    */
  def index = Action {
    Home
  }

  /**
    * Display the paginated list of products.
    *
    * @param page    Current page number (starts from 0)
    * @param orderBy Column to be sorted
    * @param filter  Filter applied on product names
    */
  def list(page: Int, orderBy: Int, filter: String) = Action.async { implicit request =>
    productService.list(page = page, orderBy = orderBy, filter = "%" + filter + "%").map { page =>
      //Ok(html.lists(page, orderBy, filter))
      Ok
    }
  }

  /**
    * Display the 'edit form' of a existing Computer.
    *
    * @param id Id of the computer to edit
    */
  def edit(id: Long) = Action.async { implicit request =>
    productService.findById(id).flatMap {
      case Some(product) =>
        imageService.options.map { options =>
          //Ok(html.editForms(id, productForm.fill(product), options))
          Ok
        }
      case other =>
        Future.successful(NotFound)
    }
  }

//  /**
//    * Handle the 'edit form' submission
//    *
//    * @param id Id of the computer to edit
//    */
//  def update(id: Long) = Action.async { implicit request =>
//    productForm.bindFromRequest.fold(
//      formWithErrors => {
//        logger.warn(s"form error: $formWithErrors")
//        productService.options.map { options =>
//          BadRequest(html.editForm(id, formWithErrors, options))
//        }
//      },
//      computer => {
//        productService.update(id, product).map { _ =>
//          Home.flashing("success" -> "Computer %s has been updated".format(computer.name))
//        }
//      }
//    )
//  }
//
//  /**
//    * Display the 'new computer form'.
//    */
//  def create = Action.async { implicit request =>
//    companyService.options.map { options =>
//      Ok(html.createForm(computerForm, options))
//    }
//  }
//
//  /**
//    * Handle the 'new computer form' submission.
//    */
//  def save = Action.async { implicit request =>
//    computerForm.bindFromRequest.fold(
//      formWithErrors => companyService.options.map { options =>
//        BadRequest(html.createForm(formWithErrors, options))
//      },
//      computer => {
//        computerService.insert(computer).map { _ =>
//          Home.flashing("success" -> "Computer %s has been created".format(computer.name))
//        }
//      }
//    )
//  }
//
//  /**
//    * Handle computer deletion.
//    */
//  def delete(id: Long) = Action.async {
//    computerService.delete(id).map { _ =>
//      Home.flashing("success" -> "Computer has been deleted")
//    }
//  }

}
            
