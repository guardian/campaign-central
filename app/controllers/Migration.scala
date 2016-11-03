package controllers

import play.api.libs.ws.WSClient
import play.api.mvc.Controller
import repositories.CampaignRepository

class Migration(override val wsClient: WSClient) extends Controller with PandaAuthActions {

  def addCampaignType() = APIAuthAction { req =>
    val campaigns = CampaignRepository.getAllCampaigns

    campaigns foreach { c =>
      val cWithType = c.copy(`type` = Some("hosted"))
      CampaignRepository.putCampaign(cWithType)
    }

    Ok(s"added type hosted to ${campaigns.length} camapaigns")
  }

}
