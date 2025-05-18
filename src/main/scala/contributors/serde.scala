package contributors

import contributors.domain.{Commit, Contributor, RepoName}
import zio.json.ast.{Json, JsonCursor}
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

object serde {
  given decodeCommit: JsonDecoder[Commit] = {
    val commitCursor = JsonCursor.field("commit") >>> JsonCursor.isObject
    val authorCursor = JsonCursor.field("author") >>> JsonCursor.isObject
    val dateCursor = commitCursor >>> authorCursor >>> JsonCursor.field("date") >>> JsonCursor.isString
    val messageCursor = commitCursor >>> JsonCursor.field("message") >>> JsonCursor.isString
    val loginCursor = authorCursor >>> JsonCursor.field("login") >>> JsonCursor.isString
    val commitAuthorName = commitCursor >>> authorCursor >>> JsonCursor.field("name") >>> JsonCursor.isString
    JsonDecoder[Json].mapOrFail { c =>
      for {
        date <- c.get(dateCursor)
        message <- c.get(messageCursor)
        login <- c.get(loginCursor).orElse(c.get(commitAuthorName))
      } yield Commit(login.value, message.value, date.value)
    }
  }
  given decodeCommits: JsonDecoder[List[Commit]] = JsonDecoder.list[Commit]

  given encodeCommit: JsonEncoder[Commit] = DeriveJsonEncoder.gen[Commit]
  given encodeCommits: JsonEncoder[List[Commit]] = JsonEncoder.list[Commit]

  given decodeRepoName: JsonDecoder[RepoName] = {
    val nameCursor = JsonCursor.field("name") >>> JsonCursor.isString
    JsonDecoder[Json].mapOrFail { c =>
      c.get(nameCursor).map(j => RepoName(j.value))
    }
  }
  given decodeRepoNames: JsonDecoder[List[RepoName]] = JsonDecoder.list[RepoName]

  given encodeContributor: JsonEncoder[Contributor] = DeriveJsonEncoder.gen[Contributor].contramap { c =>
    c.copy(activityTime = c.activityTime.toList.toArray)
  }
  given encodeContributors: JsonEncoder[List[Contributor]] = JsonEncoder.list[Contributor]
}
