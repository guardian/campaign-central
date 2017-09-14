package util

import cats.implicits._
import com.gu.scanamo.error.DynamoReadError
import play.api.Logger

object DynamoResults {

  // todo: use getResult
  def getResults[A](scanamoResults: List[Either[DynamoReadError, A]])(implicit logger: Logger): List[A] =
    scanamoResults flatMap {
      case Left(e) =>
        logger.error(e.show)
        None
      case Right(result) =>
        Some(result)
    }

  def getResult[A](scanamoResult: Either[DynamoReadError, A])(implicit logger: Logger): Option[A] =
    scanamoResult match {
      case Left(e) =>
        logger.error(e.show)
        None
      case Right(result) =>
        Some(result)
    }
}
