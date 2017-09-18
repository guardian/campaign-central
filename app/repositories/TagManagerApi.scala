package repositories

import java.util.concurrent.TimeUnit

import model.external.Sponsorship
import okhttp3._
import play.api.Logger
import play.api.libs.json.Json
import services.Config
import model.command.{CampaignCentralApiError, FetchTagSponsorshipFailed, JsonParsingError, SponsorshipNotFound}

object TagManagerApi {

  val httpClient = new OkHttpClient.Builder()
    .connectTimeout(2, TimeUnit.SECONDS)
    .build()

  def getSponsorshipForTag(id: Long): Either[CampaignCentralApiError, Sponsorship] = {

    Logger.info(s"connecting to ${Config().tagManagerApiUrl}/hyper/tags/$id/sponsorships")

    val req  = new Request.Builder().url(s"${Config().tagManagerApiUrl}/hyper/tags/$id/sponsorships").build
    val resp = httpClient.newCall(req).execute

    resp.code match {
      case 404 => Left(SponsorshipNotFound(s"Sponsorship for tag with id $id not found."))
      case 200 =>
        val responseJson = Json.parse(resp.body().string())
        (responseJson \ "data" \\ "data").headOption.map(_.as[Sponsorship]).map(Right(_)) getOrElse Left(
          JsonParsingError(s"Could not parse json for sponsorship: ${responseJson.toString}"))

      case c =>
        Logger.warn(s"failed to fetch tag sponsorship ${resp.body}")
        Left(FetchTagSponsorshipFailed(s"failed to fetch tag sponsorship ${resp.body}"))

    }
  }
}
