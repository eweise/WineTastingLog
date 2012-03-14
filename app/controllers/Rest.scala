package controllers

import play.api.mvc._
import play.api.mvc.Controller
import org.apache.commons.codec.binary.Base64
import java.io.{FileOutputStream, File}
import models.{User, Tasting}
import anorm.{NotAssigned, Pk}
import play.api.data._
import play.api.data.Forms._
import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._

import views._

import models._


object Rest extends Controller {

  //  val tastingForm:Form[Tasting] = Form(
  //    mapping(
  //      "id" -> ignored(NotAssigned:Pk[Long]),
  //      "userId" -> optional(longNumber),
  //      "rating" -> optional(number),
  //      "notes" -> optional(text),
  //      "brand" -> optional(text),
  //      "style" -> optional(text),
  //      "region" -> optional(text),
  //      "year" -> optional(number),
  //      "updateDate" -> optional(date)
  //    )(Tasting.apply)(Tasting.unapply)
  //  )

  val idForm = Form(
    tuple(
      "token" -> text,
      "id" -> text
    )
  )

  def delete = Action {
    implicit request =>
      println("delete called")
      Tasting.delete(formValue("id").get.toInt)
      Ok("")
  }

  def tastings = Action {
    request =>
      println("tastings")
      val token = request.queryString.get("token").get.headOption.getOrElse("")
      User.findByToken(token) match {
        case None => BadRequest("no user found")
        case user: Some[User] => {
          println("found user " + user.get.id + " with token " + token)
          Ok(Tasting.listAsJson(user.get.id.get))
        }
      }
  }

  def getimage = Action {
    request =>
      println("getImage")
      val token = request.queryString.get("token").get.headOption.getOrElse("")
      println("getImage token = "+token)
      User.findByToken(token) match {
        case None => BadRequest("no user found")
        case user: Some[User] => {
          println("getImage found user")

          val tastingId = request.queryString.get("tastingid").get.headOption.getOrElse("")
          println("found tasting " + tastingId)
          val image = Image.read(tastingId)
          image match {
            case None => Ok("")
            case _ => val base64Image = Base64.encodeBase64(image.get)
            println(base64Image)
            Ok(base64Image)
          }
        }
      }
  }

  def getUser(implicit request: Request[AnyContent]): Option[User] = {
    val token = formValue("token").getOrElse("")
    println("token = " + token)
    User.findByToken(token)
  }


  def formValue(key: String)(implicit request: Request[AnyContent]): Option[String] = {
    val form = request.body.asFormUrlEncoded.get
    println("formvalue key " + key)
    val value = form.get(key)
    println("formvalue value = " + value)
    value match {
      case s: Some[Seq[String]] => {
        println("formvalue some = " + value.get.headOption)
        value.get.headOption
      }
      case _ => {
        println("formvalue None")
        None
      }
    }
  }


  def saveTasting = Action {
    implicit request =>
      println("save tasting called")
      val user = getUser

      if (user == None) {
        BadRequest("no user found")
      }

      val userId = Some(user.get.id.get)
      val brand = formValue("brand")
      val style = formValue("style")
      val region = formValue("region")
      val year = Some(formValue("year").getOrElse("2000").toInt)
      val notes = formValue("notes")
      val rating = Some(formValue("rating").getOrElse("1").toInt);

      val tasting = new Tasting(NotAssigned, userId, rating, notes, brand, style, region, year, None)
      val id = formValue("id")

      id match {
        case None => {
          Tasting.insert(tasting)
        }
        case _ =>
          Tasting.update(id.get.toInt, userId.get, tasting)
      }
      Ok("")
  }

  def uploadphoto = Action {
    request =>
      val message = request.body.asFormUrlEncoded.get.asInstanceOf[Map[String, List[String]]]
      println("message = " + message)
      val token = message.get("token").get.headOption.get
      val tastingId = message.get("tastingid").get.headOption.get
      User.findByToken(token) match {
        case None => BadRequest("no user found")
        case _ => {
          val imageValue = message.get("image").get.headOption.get
          println("getting bytes...")
          val bytes = Base64.decodeBase64(imageValue.getBytes)
          println("finished getting bytes")
          Image.write(tastingId, bytes)
        }
      }
      Ok(Tasting.listAsJson(1))
  }


}