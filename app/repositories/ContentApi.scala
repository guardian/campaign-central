package repositories

import java.util.concurrent.Executors

import com.gu.contentapi.client.ContentApiClientLogic
import com.gu.contentapi.client.model._
import com.gu.contentapi.client.model.v1.Content
import dispatch.FunctionHandler
import okhttp3.Credentials
import play.api.Logger
import services.Config

import scala.annotation.tailrec
import scala.concurrent.{Await, ExecutionContext, ExecutionContextExecutor, Future}
import scala.concurrent.duration._

class ContentApi(config: Config) {

  private val previewApiClient = new DraftContentApiClass(config)

  private implicit val executionContext: ExecutionContextExecutor =
    ExecutionContext.fromExecutor(Executors.newFixedThreadPool(2))

  @tailrec
  final def loadAllContentInSection(pathPrefix: String, page: Int = 1, content: List[Content] = Nil): List[Content] = {
    Logger.debug(s"Loading page $page of content for section $pathPrefix")
    val response = previewApiClient.getResponse(
      SearchQuery()
        .section(pathPrefix)
        .showAtoms("all")
        .showFields("internalComposerCode,isLive,firstPublicationDate,headline")
        .showTags("all")
        .pageSize(10)
        .page(page)
    )

    val resultPage = Await.result(response, 5.seconds)

    val allContent = content ::: resultPage.results.toList

    if (page >= resultPage.pages) {
      allContent
    } else {
      loadAllContentInSection(pathPrefix, page + 1, allContent)
    }
  }

}

class DraftContentApiClass(config: Config) extends ContentApiClientLogic() {
  override val apiKey: String    = config.capiKey
  override val targetUrl: String = config.capiPreviewUrl

  override protected def get(url: String, headers: Map[String, String])(
    implicit context: ExecutionContext): Future[HttpResponse] = {

    val headersWithAuth = headers ++ Map(
      "Authorization" -> Credentials.basic(config.capiPreviewUser, config.capiPreviewPassword))

    val req = headersWithAuth.foldLeft(dispatch.url(url)) {
      case (r, (name, value)) => r.setHeader(name, value)
    }

    def handler = new FunctionHandler(r => HttpResponse(r.getResponseBodyAsBytes, r.getStatusCode, r.getStatusText))
    http(req.toRequest, handler)
  }
}
