package services

import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.regions.Region
import com.amazonaws.regions.Regions.EU_WEST_1
import com.amazonaws.services.dynamodbv2.document.{DynamoDB, Table}
import com.amazonaws.services.dynamodbv2.{AmazonDynamoDB, AmazonDynamoDBClientBuilder}
import com.amazonaws.services.ec2.model.{DescribeTagsRequest, Filter}
import com.amazonaws.services.ec2.{AmazonEC2, AmazonEC2ClientBuilder}
import com.amazonaws.services.s3.{AmazonS3, AmazonS3ClientBuilder}
import com.amazonaws.util.EC2MetadataUtils
import play.api.Logger

import scala.collection.JavaConverters._

class AWS(val profile: String) {

  private val defaultRegion = EU_WEST_1
  lazy val region: Region   = Region.getRegion(defaultRegion)

  lazy val credentialsProvider: AWSCredentialsProvider = {
    Logger.info(s"using local aws profile $profile")
    new ProfileCredentialsProvider(profile)
  }

  lazy val EC2Client: AmazonEC2 =
    AmazonEC2ClientBuilder.standard().withRegion(defaultRegion).withCredentials(credentialsProvider).build()
  lazy val DynamoClient: AmazonDynamoDB =
    AmazonDynamoDBClientBuilder.standard().withRegion(defaultRegion).withCredentials(credentialsProvider).build()
  lazy val S3Client: AmazonS3 =
    AmazonS3ClientBuilder.standard().withRegion(defaultRegion).withCredentials(credentialsProvider).build()
}

trait AwsInstanceTags {

  def aws: AWS

  lazy val instanceId = Option(EC2MetadataUtils.getInstanceId)

  def readTag(tagName: String): Option[String] = {
    instanceId.flatMap { id =>
      val tagsResult = aws.EC2Client.describeTags(
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

class Dynamo(aws: AWS, config: Config) {
  lazy val dynamoDb = new DynamoDB(aws.DynamoClient)

  lazy val campaignTable: Table                = dynamoDb.getTable(config.campaignTableName)
  lazy val campaignNotesTable: Table           = dynamoDb.getTable(config.campaignNotesTableName)
  lazy val campaignContentTable: Table         = dynamoDb.getTable(config.campaignContentTableName)
  lazy val clientTable: Table                  = dynamoDb.getTable(config.clientTableName)
  lazy val analyticsDataCacheTable: Table      = dynamoDb.getTable(config.analyticsDataCacheTableName)
  lazy val trafficDriverRejectTable: Table     = dynamoDb.getTable(config.trafficDriverRejectTableName)
  lazy val campaignPageviewsTable: Table       = dynamoDb.getTable(config.campaignPageviewsTableName)
  lazy val campaignUniquesTable: Table         = dynamoDb.getTable(config.campaignUniquesTableName)
  lazy val latestCampaignAnalyticsTable: Table = dynamoDb.getTable(config.latestCampaignAnalyticsTableName)
  lazy val campaignReferralTable: Table        = dynamoDb.getTable(config.campaignReferralTableName)
}
