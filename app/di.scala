import controllers._
import play.api.{Application, ApplicationLoader, BuiltInComponentsFromContext}
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

  AWS.init(configuration.getString("aws.profile"))
  LogShipping.init

  val appController = new App(wsClient)
  val campaignApiController = new CampaignApi(wsClient)
  val managementController = new Management()
  val assetsController = new Assets(httpErrorHandler)

  def router: Router = new Routes(
    httpErrorHandler,
    appController,
    campaignApiController,
    assetsController,
    managementController
  )
}
