package util

import org.joda.time.DateTime

case class ValueWithExpiry[A](expires: Long, value: A)

class AnalyticsCache[A, B] {

  var internalMap = Map[A, ValueWithExpiry[B]]()

  def millisAtEndOfDay = DateTime.now().withTimeAtStartOfDay().plusDays(1).getMillis

  def get(key: A): Option[B] = {
    internalMap.get(key).flatMap { valueWithExpiry =>
      if (valueWithExpiry.expires < System.currentTimeMillis) None else Some(valueWithExpiry.value)
    }
  }

  def put(key: A, value: B): Unit = {
    internalMap = internalMap + (key -> ValueWithExpiry(millisAtEndOfDay, value))
  }

}
