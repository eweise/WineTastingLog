package controllers


import play.api.mvc._
import play.api.data._
import play.api.data.Forms._

import anorm._

import views._
import html.index
import models._
import java.io.File


object Tastings extends Controller {


  def tastings = {
    Action {
      implicit request =>
        request.session.get(User.USER_ID) match {
          case None => Redirect(routes.Application.login())
          case _ =>   Ok(html.tastings(Tasting.list(request.session.get(User.USER_ID).get.toInt))(request.session))
        }
    }
  }
}