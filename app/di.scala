import com.gu.googleauth.{AuthAction, GoogleAuthConfig}
import controllers._
import play.api.{Application, ApplicationLoader, BuiltInComponentsFromContext, Logger}
import play.api.ApplicationLoader.Context
import play.api.libs.ws.ahc.AhcWSComponents
import play.api.mvc.{AnyContent, EssentialFilter}
import play.api.routing.Router
import play.filters.HttpFiltersComponents
import services.{AWS, Config, LogShipping}
import router.Routes

import scala.util.Try

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
  AWS.init(Try(configuration.get[String]("aws.profile")).toOption)

  Logger.info("bootstrapping log shipping")
  LogShipping.init()

  Logger.info("bootstrapping controllers")

  val googleAuthConfig = GoogleAuthConfig(
    clientId = Config().googleAuthClientId,
    clientSecret = Config().googleAuthClientSecret,
    redirectUrl = Config().googleAuthRedirectUrl,
    domain = Config().googleAuthDomain
  )
  val authAction = new AuthAction[AnyContent](
    googleAuthConfig,
    routes.LogIn.logIn(),
    controllerComponents.parsers.default
  )(executionContext)

  val appController             = new App(controllerComponents, authAction)
  val campaignApiController     = new CampaignApi(controllerComponents, authAction)
  val reportExecutionController = new ReportExecutionApi(controllerComponents, authAction)
  val managementApiController   = new ManagementApi(controllerComponents)
  val healthcheckController     = new Healthcheck(controllerComponents)
  val assetsController          = new Assets(httpErrorHandler, assetsMetadata)
  val logInController           = new LogIn(wsClient, controllerComponents, googleAuthConfig)

  def router: Router = new Routes(
    httpErrorHandler,
    appController,
    logInController,
    campaignApiController,
    reportExecutionController,
    managementApiController,
    assetsController,
    healthcheckController
  )
}
