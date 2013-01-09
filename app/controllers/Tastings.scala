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

  val filterForm = Form(
    tuple(
      "brand" -> optional(text),
      "style" -> optional(text),
      "region" -> optional(text),
      "year" -> optional(number)
    )
  )


  def tastings = {
    Action {
      implicit request =>
        val query = for ((k, v) <- request.queryString) yield (k, v(0))
        val result = filterForm.bind(query).get
        val successForm = filterForm.fill(result)
        request.session.get(User.USER_ID) match {
          case None => Redirect(routes.Application.login())
          case _ => {
            var tastings = Tasting.list(request.session.get(User.USER_ID).get.toInt)
            tastings = tastings.filter {
              t =>
                val hasBrand = result._1.foldLeft(true)((a,b) => Some(b) == t.brand)
                val hasStyle = result._2.foldLeft(true)((a,b) => Some(b) == t.style)
                val hasRegion = result._3.foldLeft(true)((a,b) => Some(b) == t.region)
                val hasYear = result._4.foldLeft(true)((a,b) => Some(b) == t.year)
                hasRegion && hasStyle && hasYear && hasBrand
            }
            Ok(html.tastings(tastings, successForm)(request.session))
          }
        }
    }
  }
}