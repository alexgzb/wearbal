package controllers

import javax.inject.{Inject, Singleton}

import play.api.mvc.{BaseController, ControllerComponents}
import play.api.routing.JavaScriptReverseRouter

@Singleton
class Application @Inject()(val controllerComponents: ControllerComponents) extends BaseController{

  def javascriptRoutes() = Action { implicit request =>
   Ok(
     JavaScriptReverseRouter("jsRoutes")(
       controllers.routes.javascript.OrderController.printOrder
     )
   )
  }

}
