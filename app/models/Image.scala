package models

import java.io.File
import org.apache.commons.io.FileUtils

object Image {

  def location(userId:String, tastingId:String) = "public/images/user/" + userId + "/image" + tastingId

  def assetLoc(userId:String, tastingId:String) = "/assets/images/user/" + userId + "/image" + tastingId

}
