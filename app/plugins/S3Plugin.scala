package plugins

import play.api.{Logger, Application, Plugin}
import com.amazonaws.services.s3.{AmazonS3Client, AmazonS3}
import play.api.Logger
import com.amazonaws.auth.BasicAWSCredentials

class S3Plugin(var application: Application) extends Plugin {
  val AWS_S3_BUCKET = "aws.s3.bucket"
  val AWS_ACCESS_KEY = "aws.access.key"
  val AWS_SECRET_KEY = "aws.secret.key"

  override def onStart() {
    val configuration = application.configuration
    // you can now access the application.conf settings, including any custom ones you have added
    Logger.info("starting")
    val accessKey = application.configuration.getString(AWS_ACCESS_KEY).get
    val secretKey = application.configuration.getString(AWS_SECRET_KEY).get

    if ((accessKey != null) && (secretKey != null)) {
      val awsCredentials = new BasicAWSCredentials(accessKey, secretKey)
      S3Plugin.amazonS3 = new AmazonS3Client(awsCredentials)
      S3Plugin.amazonS3.createBucket(S3Plugin.bucketName)
      Logger.info("Using S3 Bucket: " + S3Plugin.bucketName)
    }

    Logger.info("S3Plugin has started")
  }

  override def onStop() {
    // you may want to tidy up resources here
    Logger.info("S3Plugin has stopped")
  }
}

object S3Plugin {
  val bucketName: String = "winetastinglog"

  var amazonS3: AmazonS3 = null


}
