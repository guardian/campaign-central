package repositories

import java.util.concurrent.TimeUnit

import model.external.Sponsorship
import okhttp3._
import play.api.Logger
import play.api.libs.json.Json
import services.Config

class TagManagerApi(config: Config) {

  val httpClient: OkHttpClient =
    new OkHttpClient.Builder()
      .connectTimeout(2, TimeUnit.SECONDS)
      .build()

  def getSponsorshipForTag(id: Long): Option[Sponsorship] = {

    Logger.info(s"connecting to ${config.tagManagerApiUrl}/hyper/tags/$id/sponsorships")

    val req  = new Request.Builder().url(s"${config.tagManagerApiUrl}/hyper/tags/$id/sponsorships").build
    val resp = httpClient.newCall(req).execute

    resp.code match {
      case 404 => None
      case 200 =>
        val responseJson = Json.parse(resp.body().string())
        (responseJson \ "data" \\ "data").headOption.map(_.as[Sponsorship])
      case _ =>
        Logger.warn(s"failed to fetch tag sponsorship ${resp.body}")
        throw new RuntimeException(s"failed to fetch tag sponsorship ${resp.body}")
    }
  }
}
