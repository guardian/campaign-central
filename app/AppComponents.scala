import controllers._
import play.api.{Application, ApplicationLoader, BuiltInComponentsFromContext, Logger}
import play.api.ApplicationLoader.Context
import play.api.libs.ws.ahc.AhcWSComponents
import play.api.routing.Router
import services.{AWS, LogShipping}
import router.Routes



class AppLoader extends ApplicationLoader {
  def load(context: Context): Application = {
    new AppComponents(context).application
  }
}

class AppComponents(context: Context) extends BuiltInComponentsFromContext(context) with AhcWSComponents {

  Logger.info("bootstrapping AWS")
  AWS.init(configuration.getString("aws.profile"))

  Logger.info("bootstrapping log shipping")
  LogShipping.init

  Logger.info("bootstrapping controllers")
  val appController = new App(wsClient)
  val campaignApiController = new CampaignApi(wsClient)
  val clientApiController = new ClientApi(wsClient)
  val managementApiController = new ManagementApi(wsClient)
  val managementController = new Management()
  val assetsController = new Assets(httpErrorHandler)

  def router: Router = new Routes(
    httpErrorHandler,
    appController,
    campaignApiController,
    clientApiController,
    managementApiController,
    assetsController,
    managementController
  )
}
