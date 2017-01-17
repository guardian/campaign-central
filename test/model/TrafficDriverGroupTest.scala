package model

import java.time.LocalDate

import org.scalatest.{FlatSpec, Matchers}

class TrafficDriverGroupTest extends FlatSpec with Matchers {

  "fromTrafficDrivers" should "work for 0 drivers" in {
    val trafficDrivers = Nil
    TrafficDriverGroup.fromTrafficDrivers("grp", trafficDrivers) shouldBe None
  }

  it should "work for 1 driver" in {
    val trafficDrivers = Seq(
      TrafficDriver(
        id = 1,
        name = "name",
        url = "url",
        status = "status",
        startDate = LocalDate.of(2015, 1, 2),
        endDate = Some(LocalDate.of(2015, 4, 6)),
        summaryStats = PerformanceStats(12, 3)
      )
    )
    val grp = TrafficDriverGroup.fromTrafficDrivers("grp", trafficDrivers)
    grp shouldBe Some(
      TrafficDriverGroup(
        groupName = "grp",
        startDate = LocalDate.of(2015, 1, 2),
        endDate = LocalDate.of(2015, 4, 6),
        summaryStats = PerformanceStats(12, 3),
        trafficDriverUrls = Seq("url")
      )
    )
  }

  it should "work for many drivers" in {
    val trafficDrivers = Seq(
      TrafficDriver(
        id = 1,
        name = "name1",
        url = "url1",
        status = "status",
        startDate = LocalDate.of(2015, 1, 2),
        endDate = Some(LocalDate.of(2017, 4, 6)),
        summaryStats = PerformanceStats(12, 3)
      ),
      TrafficDriver(
        id = 2,
        name = "name2",
        url = "url2",
        status = "status",
        startDate = LocalDate.of(2014, 3, 22),
        endDate = Some(LocalDate.of(2016, 12, 6)),
        summaryStats = PerformanceStats(2, 1)
      ),
      TrafficDriver(
        id = 3,
        name = "name3",
        url = "url3",
        status = "status",
        startDate = LocalDate.of(2015, 5, 7),
        endDate = Some(LocalDate.of(2015, 5, 16)),
        summaryStats = PerformanceStats(127, 43)
      ),
      TrafficDriver(
        id = 4,
        name = "name4",
        url = "url4",
        status = "status",
        startDate = LocalDate.of(2015, 6, 7),
        endDate = Some(LocalDate.of(2015, 8, 16)),
        summaryStats = PerformanceStats(0, 0)
      )
    )

    val grp = TrafficDriverGroup.fromTrafficDrivers("grp", trafficDrivers)

    grp shouldBe Some(
      TrafficDriverGroup(
        groupName = "grp",
        startDate = LocalDate.of(2014, 3, 22),
        endDate = LocalDate.of(2017, 4, 6),
        summaryStats = PerformanceStats(141, 47),
        trafficDriverUrls = Seq("url1", "url2", "url3", "url4")
      )
    )
  }
}
