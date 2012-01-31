package controllers

import play.api.mvc.{Security, Controller}
import models.Tasting


object Rest extends Controller {



  def tastings = Action {
    Ok(Tasting.listAsJson(1L))
  }

}