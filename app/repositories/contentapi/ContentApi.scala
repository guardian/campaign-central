package repositories.contentapi

import java.io.IOException
import java.util.concurrent.Executors
import com.gu.contentapi.client.ContentApiClientLogic
import com.gu.contentapi.client.model._
import com.gu.contentapi.client.model.v1.{Content, Section}
import okhttp3._
import play.api.Logger
import services.Config
import scala.collection.JavaConverters._
import scala.annotation.tailrec
import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future, Promise}

object ContentApi {

  private val previewApiClient = new DraftContentApiClass(Config().capiKey)

  private val executorService           = Executors.newFixedThreadPool(2)
  private implicit val executionContext = ExecutionContext.fromExecutor(executorService)

  def getSectionsWithPaidContentSponsorship(): Seq[Section] = {
    val query      = SectionsQuery().sponsorshipType("paid-content")
    val response   = previewApiClient.getResponse(query)
    val resultPage = Await.result(response, 5.seconds)
    val sections   = resultPage.results
    sections
  }

  def getSection(sectionId: String): Option[Section] = {
    val query      = ItemQuery(sectionId)
    val response   = previewApiClient.getResponse(query)
    val resultPage = Await.result(response, 5.seconds)
    resultPage.section
  }

  @tailrec
  def loadAllContentInSection(pathPrefix: String, page: Int = 1, content: List[Content] = Nil): List[Content] = {
    Logger.debug(s"Loading page ${page} of content for section ${pathPrefix}")
    val response = previewApiClient.getResponse(
      new SearchQuery()
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

class DraftContentApiClass(override val apiKey: String) extends ContentApiClientLogic() {
  override val targetUrl = Config().capiPreviewUrl

  private val client: OkHttpClient = new OkHttpClient

  private val authHeaders = Map(
    "Authorization" -> Credentials.basic(Config().capiPreviewUser, Config().capiPreviewPassword))

  override protected def get(url: String, headers: Map[String, String])(
    implicit context: ExecutionContext): Future[HttpResponse] = {

    val promise          = Promise[HttpResponse]()
    val request: Request = new Request.Builder().url(url).headers(Headers.of((headers ++ authHeaders).asJava)) build ()

    client
      .newCall(request)
      .enqueue(
        new Callback() {
          override def onFailure(call: Call, e: IOException) { promise.failure(e) }
          override def onResponse(call: Call, response: Response) {
            promise.success(HttpResponse(response.body().bytes(), response.code, response.message))
          }
        }
      )

    promise.future
  }
}
