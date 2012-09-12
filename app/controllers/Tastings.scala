package controllers


import play.api.mvc._
import play.api.data._
import play.api.data.Forms._

import anorm._

import views._
import models._


object Tastings extends Controller {


  def tastings = {
    Action {
      implicit request =>
        Ok(html.tastings(Tasting.list(request.session.get(User.USER_ID).get.toInt))(request.session))
    }
  }


}