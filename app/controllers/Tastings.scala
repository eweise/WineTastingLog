package controllers


import play.api.mvc._
import play.api.data._
import play.api.data.Forms._

import anorm._

import views._
import html.index
import models._
import java.io.File
import scala.Some
import scala.Some


object Tastings extends Controller {

  val filterForm = Form(
    tuple(
      "brand" -> optional(text),
      "style" -> optional(text),
      "region" -> optional(text),
      "year" -> optional(number)
    )
  )


  def tastings = {
    Security.Authenticated {
      user =>
        Action {
          implicit request =>
            val form = filterForm.bindFromRequest()
            val (brand, style, region, year) = form.get
            val filteredResult = Tasting.list(user.toLong).filter {
              t =>
                brand.foldLeft(true)((a, b) => Some(b) == t.brand) &&
                  style.foldLeft(true)((a, b) => Some(b) == t.style) &&
                  region.foldLeft(true)((a, b) => Some(b) == t.region) &&
                  year.foldLeft(true)((a, b) => Some(b) == t.year)
            }
            Ok(html.tastings(filteredResult, form))
        }
    }
  }

}
