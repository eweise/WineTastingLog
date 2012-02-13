package controllers

import play.api.mvc._
import play.api.mvc.Controller
import models.Tasting
import org.apache.commons.codec.binary.Base64
import scalax.io.support.FileUtils
import java.io.{FileOutputStream, File}


object Rest extends Controller {


  def tastings = Action {
    request =>
      val token = request.queryString.get("token").get.headOption

      token match {
        case None => BadRequest("no token supplied")
        case _ =>
          println("token = " + token)
          Ok(Tasting.listAsJson(1))
      }
  }

  def uploadphoto = Action {
    request =>
      val token = request.queryString.get("token").get.headOption
      token match {
        case None => BadRequest("no token supplied")
        case _ =>
          println("before multipart")
          val body = request.body.asFormUrlEncoded

          body.foreach {
            m =>
              m.get("image").get.headOption match {
                case s: Some[String] =>
                  println("getting bytes...")
                  val bytes = Base64.decodeBase64(s.get.getBytes)
                  println("finished getting bytes")

                  val f = new File("test.jpg")
                  val fos = new FileOutputStream(f)
                  fos.write(bytes)
                  fos.close()
                case _ =>

              }
          }
      }
      Ok(Tasting.listAsJson(1))
  }


}