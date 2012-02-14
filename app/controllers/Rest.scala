package controllers

import play.api.mvc._
import play.api.mvc.Controller
import org.apache.commons.codec.binary.Base64
import java.io.{FileOutputStream, File}
import models.{User, Tasting}


object Rest extends Controller {

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

  def uploadphoto = Action {
    request =>
      val message = request.body.asFormUrlEncoded.get.asInstanceOf[Map[String, List[String]]]
      println("message = " + message)
      val token = message.get("token").get.headOption.get
      User.findByToken(token) match {
        case None => BadRequest("no user found")
        case _ => {
          val imageValue = message.get("image").get.headOption.get
          println("getting bytes...")
          val bytes = Base64.decodeBase64(imageValue.getBytes)
          println("finished getting bytes")
          writeToFile(bytes)
        }
      }
      Ok(Tasting.listAsJson(1))
  }

  def writeToFile(bytes: Array[Byte]) {
    val fos = new FileOutputStream(new File("test.jpg"))
    fos.write(bytes)
    fos.close()
  }


}