import controllers._
import play.api.{Application, ApplicationLoader, BuiltInComponentsFromContext, Logger}
import play.api.ApplicationLoader.Context
import play.api.libs.ws.ahc.AhcWSComponents
import play.api.mvc.EssentialFilter
import play.api.routing.Router
import play.filters.HttpFiltersComponents
import services.{AWS, LogShipping}
import router.Routes

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
  AWS.init(configuration.get[String]("aws.profile"))

  Logger.info("bootstrapping log shipping")
  LogShipping.init

  Logger.info("bootstrapping controllers")

  val appController = new App(wsClient, controllerComponents)
  val campaignApiController = new CampaignApi(wsClient, controllerComponents)
  val clientApiController = new ClientApi(wsClient, controllerComponents)
  val managementApiController = new ManagementApi(wsClient, controllerComponents)
  val migrationController = new Migration(wsClient, controllerComponents)
  val managementController = new Management(controllerComponents)
  val assetsController = new Assets(httpErrorHandler, assetsMetadata)

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
