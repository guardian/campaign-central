package services

import com.amazonaws.auth.{AWSCredentialsProvider, DefaultAWSCredentialsProviderChain}
import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.regions.{Region, Regions}
import com.amazonaws.services.dynamodbv2.document.{DynamoDB, Table}
import com.amazonaws.services.dynamodbv2.{AmazonDynamoDB, AmazonDynamoDBClientBuilder}
import com.amazonaws.services.ec2.model.{DescribeTagsRequest, Filter}
import com.amazonaws.services.ec2.{AmazonEC2, AmazonEC2ClientBuilder}
import com.amazonaws.services.s3.{AmazonS3, AmazonS3ClientBuilder}
import com.amazonaws.util.EC2MetadataUtils
import play.api.Logger

import scala.collection.JavaConverters._

object AWS {

  private val theRegion   = Regions.EU_WEST_1
  lazy val region: Region = Region getRegion theRegion

  var creds: AWSCredentialsProvider = _

  def init(profile: Option[String]): Unit = {
    creds = profile map { p =>
      Logger.info(s"using local aws profile $p")
      new ProfileCredentialsProvider(p)
    } getOrElse {
      Logger.info("using default AWS profile")
      new DefaultAWSCredentialsProviderChain()
    }
  }

  def credentialsProvider: AWSCredentialsProvider = creds

  lazy val EC2Client: AmazonEC2 =
    AmazonEC2ClientBuilder.standard().withRegion(theRegion).withCredentials(credentialsProvider).build()
  lazy val DynamoClient: AmazonDynamoDB =
    AmazonDynamoDBClientBuilder.standard().withRegion(theRegion).withCredentials(credentialsProvider).build()
  lazy val S3Client: AmazonS3 =
    AmazonS3ClientBuilder.standard().withRegion(theRegion).withCredentials(credentialsProvider).build()
}

trait AwsInstanceTags {
  lazy val instanceId = Option(EC2MetadataUtils.getInstanceId)

  def readTag(tagName: String): Option[String] = {
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

  lazy val campaignTable: Table                = dynamoDb.getTable(Config().campaignTableName)
  lazy val campaignNotesTable: Table           = dynamoDb.getTable(Config().campaignNotesTableName)
  lazy val campaignContentTable: Table         = dynamoDb.getTable(Config().campaignContentTableName)
  lazy val clientTable: Table                  = dynamoDb.getTable(Config().clientTableName)
  lazy val analyticsDataCacheTable: Table      = dynamoDb.getTable(Config().analyticsDataCacheTableName)
  lazy val campaignPageviewsTable: Table       = dynamoDb.getTable(Config().campaignPageviewsTableName)
  lazy val campaignUniquesTable: Table         = dynamoDb.getTable(Config().campaignUniquesTableName)
  lazy val latestCampaignAnalyticsTable: Table = dynamoDb.getTable(Config().latestCampaignAnalyticsTableName)
  lazy val campaignReferralTable: Table        = dynamoDb.getTable(Config().campaignReferralTableName)
}
