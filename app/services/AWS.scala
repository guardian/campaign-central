package services

import com.amazonaws.auth.{AWSCredentialsProvider, DefaultAWSCredentialsProviderChain}
import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.regions.{Region, Regions}
import com.amazonaws.services.ec2.AmazonEC2Client
import com.amazonaws.services.ec2.model.{DescribeTagsRequest, Filter}
import com.amazonaws.util.EC2MetadataUtils
import scala.collection.JavaConverters._


object AWS {

  lazy val region = Region getRegion Regions.EU_WEST_1

  var creds: AWSCredentialsProvider = null

  def init(profile: Option[String]): Unit = {
    creds = profile map {p =>
      new ProfileCredentialsProvider(p)
    } getOrElse(
      new DefaultAWSCredentialsProviderChain()
      )
  }

  def credentialsProvider = creds

  lazy val EC2Client = region.createClient(classOf[AmazonEC2Client], creds, null)

}

trait AwsInstanceTags {
  lazy val instanceId = Option(EC2MetadataUtils.getInstanceId)

  def readTag(tagName: String) = {
    instanceId.flatMap { id =>
      val tagsResult = AWS.EC2Client.describeTags(
        new DescribeTagsRequest().withFilters(
          new Filter("resource-type").withValues("instance"),
          new Filter("resource-id").withValues(id),
          new Filter("key").withValues(tagName)
        )
      )
      tagsResult.getTags.asScala.find(_.getKey == tagName).map(_.getValue)
    }
  }
}
