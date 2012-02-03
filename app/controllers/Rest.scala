package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import play.api.mvc.{Security, Controller}
import models.Tasting


object Rest extends Controller {



  def tastings = Action {
    Ok(Tasting.listAsJson(1))
  }

}