package models

import com.amazonaws.services.s3.model.{ObjectMetadata, CannedAccessControlList, PutObjectRequest}

import java.io.File
import java.net.URL
import plugins.S3Plugin

class S3File(val userId: Long, val filename: String) {

  def getUrl: URL = new URL(<s>https://s3.amazonaws.com/{S3Plugin.bucketName}/{filePath}</s>.text)

  def filePath = <s>user/{userId}/{filename}.jpg</s>.text

  def save(file: File) {
        val metadata = new ObjectMetadata()
        metadata.setContentType("image/jpeg")
        val putObjectRequest = new PutObjectRequest(S3Plugin.bucketName, filePath, file)
        putObjectRequest.withCannedAcl(CannedAccessControlList.PublicRead)
        putObjectRequest.setMetadata(metadata)
        S3Plugin.amazonS3.putObject(putObjectRequest)
  }

  def delete(): Unit = S3Plugin.amazonS3.deleteObject(S3Plugin.bucketName, filePath)
}

object S3File {
  def apply(userId: Long, id: String) = new S3File(userId, id)
}


