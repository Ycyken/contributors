package contributors

import contributors.domain.{Commit, Contributor}
import contributors.service.{hourFromIsoTime, processCommits}

class Service extends munit.FunSuite {
  test("hour from ISO time is correct") {
    val isoTime1 = "2025-05-20T20:25:17Z"
    val isoTime2 = "2025-04-30T00:04:15Z"
    val isoTime3 = "2021-04-30T23:43:12Z"

    val hour1 = hourFromIsoTime(isoTime1)
    val hour2 = hourFromIsoTime(isoTime2)
    val hour3 = hourFromIsoTime(isoTime3)
    assertEquals(hour1, 20)
    assertEquals(hour2, 0)
    assertEquals(hour3, 23)
  }

  test("process commits is correct") {
    val commits = List(
      Commit("author1", "1234567891", "2025-05-20T20:25:17Z"),
      Commit("author2", "123456", "2025-05-20T23:11:17Z"),
      Commit("author1", "1234", "2025-05-20T01:44:00Z"),
    )

    val times1 = Array.fill(24)(0)
    times1(20) = 1
    times1(1) = 1
    val times2 = Array.fill(24)(0)
    times2(23) = 1
    val expected = List(Contributor("author1", 2, times1, 7), Contributor("author2", 1, times2, 6))

    val actual = processCommits(commits)
    assertContributorsEquals(expected, actual)

  }

  def assertContributorsEquals(list1: List[Contributor], list2: List[Contributor]): Unit = {
    assertEquals(list1.length, list2.length)
    list1.zip(list2).foreach { (l, r) =>
      assertEquals(l.name, r.name)
      assertEquals(l.commitCount, r.commitCount)
      assert(l.activityTime.sameElements(r.activityTime))
      assertEquals(l.avgCommitMsg, r.avgCommitMsg)
    }
  }

}
