package plugins

import play.api.{Application, Plugin}
import com.amazonaws.services.s3.{AmazonS3Client, AmazonS3}
import play.api.Logger
import com.amazonaws.auth.BasicAWSCredentials

class S3Plugin(var application: Application) extends Plugin {
  val AWS_S3_BUCKET = "aws.s3.bucket"
  val AWS_ACCESS_KEY = "aws.access.key"
  val AWS_SECRET_KEY = "aws.secret.key"

  override def onStart() {
    S3Plugin.amazonS3 = new AmazonS3Client(new BasicAWSCredentials(
        application.configuration.getString(AWS_ACCESS_KEY).getOrElse(throw new RuntimeException("missing " + AWS_ACCESS_KEY)),
        application.configuration.getString(AWS_SECRET_KEY).getOrElse(throw new RuntimeException("missing " + AWS_SECRET_KEY))
    ))

    S3Plugin.amazonS3.createBucket(S3Plugin.bucketName)

    Logger.info("S3Plugin has started")
  }

  override def onStop(): Unit = Logger.info("S3Plugin has stopped")
}

object S3Plugin {
  val bucketName: String = "winetastinglog"

  var amazonS3: AmazonS3 = null
}
