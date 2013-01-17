package controllers

import play.api.mvc._
import play.api.data.Form
import models.{S3File, User, Tasting}
import play.api.data.Forms._
import anorm.{Pk, NotAssigned}
import views.html
import play.api.libs.Files
import scala.Some
import Security._

object EditTasting extends Controller {

  val tastingForm: Form[Tasting] = Form(
    mapping(
      "id" -> ignored(NotAssigned: Pk[Long]),
      "userId" -> optional(longNumber),
      "rating" -> optional(number),
      "notes" -> optional(text),
      "brand" -> optional(text),
      "style" -> optional(text),
      "region" -> optional(text),
      "year" -> optional(number),
      "updateDate" -> optional(date)
    )(Tasting.apply)(Tasting.unapply)
  )

  def delete(id: Long) = Authenticated {
    user => Action {
      implicit request =>
        new S3File(user.toLong, id.toString).delete()
        Tasting.delete(id)
        Redirect(routes.Tastings.tastings)
    }
  }

  def edit(id: Long) = Authenticated {
    user => Action {
      Action {
        implicit request =>
          Ok(html.editTastings(Some(id), tastingForm.fill(Tasting.load(id).get)))
      }
    }
  }

  def newTasting() = Authenticated {
    user => Action {
      Action {
        implicit request =>
          Ok(html.editTastings(None, tastingForm))
      }
    }
  }

  def saveNew() = Authenticated {
    user => Action(parse.multipartFormData) {
      implicit request =>

        tastingForm.bindFromRequest.fold(
          formWithErrors => {
            BadRequest(html.editTastings(None, formWithErrors))
          },
          tasting => {
            tasting.userId = Some(user.toLong)
            saveFile(request, user.toLong, Tasting.insert(tasting))
            Redirect(routes.Tastings.tastings)
          }
        )
    }
  }

  def update(id: Long) = Authenticated {
    user => Action(parse.multipartFormData) {
      implicit request =>
        tastingForm.bindFromRequest.fold(
          formWithErrors => BadRequest(html.editTastings(Some(id), formWithErrors)),
          tasting => {
            Tasting.update(id, tasting)
            saveFile(request, user.toLong, id)
            Redirect(routes.Tastings.tastings)
          }
        )
    }
  }

  private[this] def saveFile(request: Request[MultipartFormData[Files.TemporaryFile]], userId: Long, tastingId: Long) {
    request.body.file("image").foreach(f => new S3File(userId, tastingId.toString).save(f.ref.file))
  }


}
