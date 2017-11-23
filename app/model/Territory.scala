package model

import model.Global.territory

/*
 * Territory and production office are tightly coupled. We only want to display campaigns from a specific production office
 * when we ask for campaign data from a specific territory.
 */
sealed trait Territory {
  val databaseKeyValue: String
  val associatedProductionOfficeValue: Option[String]
}

case object Global extends Territory {
  private val territory                               = "global"
  val databaseKeyValue: String                        = territory
  val associatedProductionOfficeValue: Option[String] = None
}

case object US extends Territory {
  private val territory                               = "us"
  val databaseKeyValue: String                        = territory.toUpperCase
  val associatedProductionOfficeValue: Option[String] = Some(territory.capitalize)
}

case object UK extends Territory {
  private val territory                               = "gb"
  val databaseKeyValue: String                        = territory.toUpperCase
  val associatedProductionOfficeValue: Option[String] = Some("Uk")
}

case object AU extends Territory {
  private val territory                               = "au"
  val databaseKeyValue: String                        = territory.toUpperCase
  val associatedProductionOfficeValue: Option[String] = Some("Aus")
}

object Territory {
  def apply(territoryValue: String): Territory = {
    territoryValue.toLowerCase match {
      case "global" => Global
      case "us"     => US
      case "gb"     => UK
      case "au"     => AU
      case _        => Global
    }
  }
}
