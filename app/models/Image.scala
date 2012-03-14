package models

import java.io.File
import org.apache.commons.io.FileUtils

object Image {

  def write(id: String, image: Array[Byte]) {
    FileUtils.writeByteArrayToFile(new File(id + ".jpg"), image)
  }

  def read(id: String): Option[Array[Byte]] = {
    val f = new File(id + ".jpg")
    f.exists() match {
      case false => {
        println("no image found for file " + f)
        None
      }
      case _ =>
        Some(FileUtils.readFileToByteArray(f))
    }
  }

}
