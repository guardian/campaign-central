import controllers._
import play.api.ApplicationLoader.Context
import play.api.libs.ws.ahc.AhcWSComponents
import play.api.mvc.EssentialFilter
import play.api.routing.Router
import play.api.{Application, ApplicationLoader, BuiltInComponentsFromContext, Logger}
import play.filters.HttpFiltersComponents
import repositories._
import router.Routes
import services._

class AppLoader extends ApplicationLoader {
  def load(context: Context): Application = {
    new AppComponents(context).application
  }
}

class AppComponents(context: Context)
  extends BuiltInComponentsFromContext(context)
  with AhcWSComponents
  with AssetsComponents
  with HttpFiltersComponents {

  // disabling some default filters until value is clear in this context
  override def httpFilters: Seq[EssentialFilter] =
    super.httpFilters.filterNot(filter => filter == csrfFilter || filter == allowedHostsFilter)

  Logger.info("bootstrapping AWS")
  val aws            = new AWS(configuration.get[String]("aws.profile"))
  val config: Config = new ConfigBuilder(aws).conf

  Logger.info("bootstrapping log shipping")
  new LogShipping(config, aws).init()

  Logger.info("bootstrapping repositories")

  val dynamo = new Dynamo(aws, config)

  val campaignContentRepository         = new CampaignContentRepository(dynamo)
  val campaignPageViewsRepository       = new CampaignPageViewsRepository(dynamo)
  val campaignNotesRepository           = new CampaignNotesRepository(dynamo)
  val latestCampaignAnalyticsRepository = new LatestCampaignAnalyticsRepository(dynamo)
  val campaignRepository                = new CampaignRepository(dynamo, campaignContentRepository, campaignNotesRepository)
  val campaignUniquesRepository         = new CampaignUniquesRepository(dynamo)
  val campaignReferralRepository        = new CampaignReferralRepository(dynamo)
  val clientRepository                  = new ClientRepository(dynamo)
  val trafficDriverRejectRepository     = new TrafficDriverRejectRepository(dynamo)
  val analyticsDataCache                = new AnalyticsDataCache(dynamo)
  val googleAnalytics                   = new GoogleAnalytics(config)
  val contentApi                        = new ContentApi(config)
  val tagManagerApi                     = new TagManagerApi(config)

  val campaignService = new CampaignService(
    campaignRepository,
    campaignPageViewsRepository,
    latestCampaignAnalyticsRepository,
    campaignUniquesRepository
  )

  Logger.info("bootstrapping controllers")

  val appController = new App(wsClient, controllerComponents, aws, config)
  val campaignApiController = new CampaignApi(
    wsClient,
    controllerComponents,
    aws,
    config,
    campaignService,
    campaignRepository,
    campaignContentRepository,
    campaignNotesRepository,
    campaignReferralRepository,
    clientRepository,
    trafficDriverRejectRepository,
    analyticsDataCache,
    googleAnalytics,
    contentApi,
    tagManagerApi
  )
  val clientApiController = new ClientApi(wsClient, controllerComponents, aws, config, clientRepository)
  val managementApiController = new ManagementApi(
    wsClient,
    controllerComponents,
    aws,
    config,
    campaignRepository,
    campaignContentRepository,
    analyticsDataCache,
    googleAnalytics,
    contentApi,
    tagManagerApi
  )
  val migrationController =
    new Migration(wsClient, controllerComponents, aws, config, campaignRepository, analyticsDataCache, googleAnalytics)
  val managementController = new Management(controllerComponents)
  val assetsController     = new Assets(httpErrorHandler, assetsMetadata)

  def router: Router = new Routes(
    httpErrorHandler,
    appController,
    campaignApiController,
    clientApiController,
    managementApiController,
    assetsController,
    migrationController,
    managementController
  )
}
