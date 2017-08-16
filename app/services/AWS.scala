package services

import com.amazonaws.auth.{AWSCredentialsProvider, DefaultAWSCredentialsProviderChain}
import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.regions.{Region, Regions}
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.amazonaws.services.ec2.AmazonEC2Client
import com.amazonaws.services.ec2.model.{DescribeTagsRequest, Filter}
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.util.EC2MetadataUtils
import play.api.Logger

import scala.collection.JavaConverters._


object AWS {

  lazy val region = Region getRegion Regions.EU_WEST_1

  var creds: AWSCredentialsProvider = null

  def init(profile: Option[String]): Unit = {
    creds = profile map {p =>
      Logger.info(s"using local aws profile $p")
      new ProfileCredentialsProvider(p)
    } getOrElse{
      Logger.info("using default AWS profile")
      new DefaultAWSCredentialsProviderChain()
    }
  }

  def credentialsProvider = creds

  lazy val EC2Client = region.createClient(classOf[AmazonEC2Client], creds, null)
  lazy val DynamoClient = AWS.region.createClient(classOf[AmazonDynamoDBClient], creds, null)
  lazy val S3Client  = region.createClient(classOf[AmazonS3Client], creds, null)

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

object Dynamo {
  lazy val dynamoDb = new DynamoDB(AWS.DynamoClient)

  lazy val campaignTable = dynamoDb.getTable(Config().campaignTableName)
  lazy val campaignNotesTable = dynamoDb.getTable(Config().campaignNotesTableName)
  lazy val campaignContentTable = dynamoDb.getTable(Config().campaignContentTableName)
  lazy val clientTable = dynamoDb.getTable(Config().clientTableName)
  lazy val analyticsDataCacheTable = dynamoDb.getTable(Config().analyticsDataCacheTableName)
  lazy val trafficDriverRejectTable = dynamoDb.getTable(Config().trafficDriverRejectTableName)

  lazy val campaignPageviewsTable = dynamoDb.getTable(Config().campaignPageviewsTableName)
  lazy val campaignUniquesTable = dynamoDb.getTable(Config().campaignUniquesTableName)
  lazy val latestCampaignAnalyticsTable = dynamoDb.getTable(Config().latestCampaignAnalyticsTableName)

}
