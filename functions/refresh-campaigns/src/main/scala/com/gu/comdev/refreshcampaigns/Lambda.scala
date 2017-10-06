package com.gu.comdev.refreshcampaigns

import okhttp3.{ OkHttpClient, Request, RequestBody, Response }

object Lambda {

  private val httpClient: OkHttpClient = new OkHttpClient()

  private val refreshCampaignsRequest: Request = {
    val configApiKey = "campaign_central_api_key"
    val configCampaignCentralUrlKey = "campaign_central_url"
    val apiKey = sys.env.getOrElse(configApiKey, sys.error(s"Could not retrieve value for key $configApiKey. Campaigns will not be refreshed."))
    val campaignCentralUrl = sys.env.getOrElse(configCampaignCentralUrlKey, sys.error(s"Could not retrieve value for key $configCampaignCentralUrlKey. Campaigns will not be refreshed."))
    val refreshCampaignsUrl = s"$campaignCentralUrl?api-key=$apiKey"

    val emptyRequestBody = RequestBody.create(null, "")
    new Request.Builder().url(refreshCampaignsUrl).post(emptyRequestBody).build()
  }

  def handleRequest(): Unit = {
    val response: Response = httpClient.newCall(refreshCampaignsRequest).execute()
    if (response.isSuccessful) {
      println("Campaigns refreshed.")
    } else if (response.code == 401 || response.code == 403) {
      sys.error("Campaigns refresh failed. It looks like the api key used may be incorrect.")
    } else {
      sys.error(s"Campaigns refresh failed with a response code of ${response.code}")
    }

  }
}
