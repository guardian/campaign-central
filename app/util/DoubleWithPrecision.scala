package util

object DoubleUtils {
  implicit class DoubleWithPrecision(val value: Double) {
    def to2Dp: Double = BigDecimal(value).setScale(2, BigDecimal.RoundingMode.HALF_UP).toDouble
  }
}

