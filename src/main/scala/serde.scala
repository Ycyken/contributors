import cats.instances.either._
import cats.syntax.all._
import domain.{Commit, Repos}
import zio.json.ast.{Json, JsonCursor}
import zio.json.{DeriveJsonDecoder, JsonDecoder}

object serde {
  private val arrayCursor = JsonCursor.isArray

  given decodeCommit: JsonDecoder[Commit] = {
    val commitCursor = JsonCursor.field("commit") >>> JsonCursor.isObject
    val authorCursor = JsonCursor.field("author") >>> JsonCursor.isObject
    val dateCursor = JsonCursor.field("date") >>> JsonCursor.isString
    val messageCursor = JsonCursor.field("message") >>> JsonCursor.isString
    val loginCursor = JsonCursor.field("login") >>> JsonCursor.isString
    JsonDecoder[Json].mapOrFail { c =>
      for {
        commit <- c.get(commitCursor)
        date <- commit.get(authorCursor).flatMap(_.get(dateCursor))
        message <- commit.get(messageCursor)
        login <- c.get(loginCursor)
      } yield Commit(login.value, message.value, date.value)
    }
  }

  given decodeCommits: JsonDecoder[List[Commit]] = DeriveJsonDecoder.gen[List[Commit]]

  given decodeRepos: JsonDecoder[Repos] = {
    val nameCursor = JsonCursor.field("name") >>> JsonCursor.isString
    JsonDecoder[Json].mapOrFail { c =>
      for {
        arr <- c.get(arrayCursor)
        names <- arr.elements.toList.traverse(_.get(nameCursor).map(_.value))
      } yield Repos(names)
    }
  }
}
