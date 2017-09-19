package util

import cats.implicits._
import com.gu.scanamo.error.DynamoReadError
import play.api.Logger

object DynamoResults {

  def getResults[A](scanamoResults: List[Either[DynamoReadError, A]])(implicit logger: Logger): List[A] =
    scanamoResults flatMap { getResult(_) }

  def getResultsOrFirstFailure[A](scanamoResults: List[Either[DynamoReadError, A]]): Either[DynamoReadError, List[A]] =
    scanamoResults collectFirst {
      case Left(e) => e
    } toLeft {
      scanamoResults collect {
        case Right(result) => result
      }
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
