package services

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.classic.{Logger => LogbackLogger}
import com.gu.logback.appender.kinesis.KinesisAppender
import net.logstash.logback.layout.LogstashLayout
import net.logstash.logback.marker.Markers._
import org.slf4j.{LoggerFactory, Logger => SLFLogger}
import play.api.Logger

object LogShipping extends AwsInstanceTags {
  val rootLogger: LogbackLogger = LoggerFactory.getLogger(SLFLogger.ROOT_LOGGER_NAME).asInstanceOf[LogbackLogger]

  def init(): Unit = {
    rootLogger.info("bootstrapping kinesis appender if configured correctly")
    for (stack      <- readTag("Stack");
         app        <- readTag("App");
         stage      <- readTag("Stage");
         streamName <- Config().logShippingStreamName) {

      Logger.info(s"bootstrapping kinesis appender with $stack -> $app -> $stage")
      val context = rootLogger.getLoggerContext

      val layout = new LogstashLayout()
      layout.setContext(context)
      layout.setCustomFields(s"""{"stack":"$stack","app":"$app","stage":"$stage"}""")
      layout.start()

      val appender = new KinesisAppender[ILoggingEvent]()
      appender.setBufferSize(1000)
      appender.setRegion(AWS.region.getName)
      appender.setStreamName(streamName)
      appender.setContext(context)
      appender.setLayout(layout)

      appender.start()

      rootLogger.addAppender(appender)
      rootLogger.info("Configured kinesis appender")
    }
  }

  def logMessageAndCustomField(message: String, fieldName: String, fieldValue: String)(implicit logger: Logger): Unit =
    logger.underlyingLogger.info(append(fieldName, fieldValue), message)
}
