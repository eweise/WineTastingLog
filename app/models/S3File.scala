package models

import com.amazonaws.services.s3.model.{ObjectMetadata, CannedAccessControlList, PutObjectRequest}
import play.Logger

import java.io.{ByteArrayInputStream, InputStream, File}
import java.net.URL
import java.util.UUID
import plugins.{S3Plugin}

class S3File(val userId:Long, val filename:String)  {

  def getUrl:URL = new URL(<s>https://s3.amazonaws.com/{S3Plugin.bucketName}/{filePath}</s>.text)

  def filePath = <s>user/{userId}/{filename}.jpg</s>.text

  def save(file:File) {
    if (S3Plugin.amazonS3 == null) {
      Logger.error("Could not save because amazonS3 was null")
      throw new RuntimeException("Could not save")
    }
    else {
      val metadata = new ObjectMetadata()
      metadata.setContentType("image/jpeg")
      val putObjectRequest = new PutObjectRequest(S3Plugin.bucketName, filePath, file)
      putObjectRequest.withCannedAcl(CannedAccessControlList.PublicRead) // public for all
      putObjectRequest.setMetadata(metadata)
      S3Plugin.amazonS3.putObject(putObjectRequest) // upload file
    }
  }

  def delete() {
    if (S3Plugin.amazonS3 == null) {
      Logger.error("Could not delete because amazonS3 was null")
      throw new RuntimeException("Could not delete")
    }
    else {
      S3Plugin.amazonS3.deleteObject(S3Plugin.bucketName, filename)
    }
  }
}

object S3File {
  def apply(userId:Long, id:String) = new S3File(userId, id)


}


