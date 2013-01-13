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
            val boundForm = filterForm.bindFromRequest()
            boundForm.fold(
              formWithErrors => Redirect(routes.Tastings.tastings),
              query => {
                val filteredResult = Tasting.list(user.toLong).filter {
                  t =>
                    query._1.foldLeft(true)((a, b) => Some(b) == t.brand) &&
                      query._2.foldLeft(true)((a, b) => Some(b) == t.style) &&
                      query._3.foldLeft(true)((a, b) => Some(b) == t.region) &&
                      query._4.foldLeft(true)((a, b) => Some(b) == t.year)
                }
                Ok(html.tastings(filteredResult, boundForm))
              }

            )
        }
    }
  }

}
