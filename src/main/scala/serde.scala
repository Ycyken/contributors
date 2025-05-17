import cats.instances.either._
import cats.syntax.all._
import domain.{Commit, Repos}
import zio.json.ast.{Json, JsonCursor}
import zio.json.{DeriveJsonEncoder, JsonDecoder, JsonEncoder}

object serde {
  private val arrayCursor = JsonCursor.isArray

  given decodeCommit: JsonDecoder[Commit] = {
    val commitCursor = JsonCursor.field("commit") >>> JsonCursor.isObject
    val authorCursor = JsonCursor.field("author") >>> JsonCursor.isObject
    val dateCursor = commitCursor >>> authorCursor >>> JsonCursor.field("date") >>> JsonCursor.isString
    val messageCursor = commitCursor >>> JsonCursor.field("message") >>> JsonCursor.isString
    val loginCursor = authorCursor >>> JsonCursor.field("login") >>> JsonCursor.isString
    JsonDecoder[Json].mapOrFail { c =>
      for {
        date <- c.get(dateCursor)
        message <- c.get(messageCursor)
        login <- c.get(loginCursor)
      } yield Commit(login.value, message.value, date.value)
    }
  }

  given decodeCommits: JsonDecoder[List[Commit]] = JsonDecoder.list[Commit]

  given encodeCommit: JsonEncoder[Commit] = DeriveJsonEncoder.gen[Commit]
  given encodeCommits: JsonEncoder[List[Commit]] = JsonEncoder.list[Commit]

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
