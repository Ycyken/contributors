package contributors

import contributors.domain.{Commit, Contributor}

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import scala.collection.mutable

object service {

  private[contributors] def hourFromIsoTime(t: String): Int = {
    val zdt = ZonedDateTime.parse(t, DateTimeFormatter.ISO_DATE_TIME)
    zdt.getHour
  }

  def processCommits(commits: List[Commit]): List[Contributor] = {
    val contributors = mutable.HashMap[String, Contributor]()
    commits.foreach(commit =>
      contributors.updateWith(commit.author) { contr =>
        val hour = hourFromIsoTime(commit.time)
        contr match
          case Some(contributor: Contributor) =>
            val times = contributor.activityTime
            times(hour) += 1
            // hack: sum up total message lengths to calculate average length in the end by commits count
            val totalMsgLen = contributor.avgCommitMsg + commit.message.length
            Some(
              contributor.copy(
                commitCount = contributor.commitCount + 1,
                activityTime = times,
                avgCommitMsg = totalMsgLen,
              ),
            )
          case None =>
            val times = Array.fill(24)(0)
            times(hour) = 1
            Some(Contributor(commit.author, 1, times, commit.message.length))
      },
    )

    // calculate avg message length by commits count
    contributors.keys.foreach { author =>
      contributors.updateWith(author)(_.map { c =>
        c.copy(avgCommitMsg = c.avgCommitMsg / c.commitCount)
      })
    }
    contributors.values.toList.sortBy(-_.commitCount)
  }

}
