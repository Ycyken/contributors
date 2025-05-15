import cats.effect.IO
import config.Token
import org.http4s.{AuthScheme, Credentials, Headers, MediaType, Method, Request, Uri}
import org.http4s.client.Client
import org.http4s.headers.{Accept, Authorization}
import org.typelevel.log4cats.syntax.LoggerInterpolator
import play.api.libs.json.{Json, Reads, Writes}
import org.typelevel.log4cats.Logger
import syntax.*

object syntax {
  extension (self: String) def into[A](using r: Reads[A]): A = Json.parse(self).as[A]
  extension [A](self: A) def toJson(using w: Writes[A]): String = Json.prettyPrint(w.writes(self))
}

object client {

  given logger: Logger[IO] = Main.logger

  def fetch[A: Reads](client: Client[IO], uri: Uri, default: => A)(using token: Token): IO[A] =
    client
      .expect[String](req(uri))
      .map(_.into[A])
      .handleErrorWith(_ => error"return default value: $default for $uri in fetch" as default)

  def req(uri: Uri)(using token: Token): Request[IO] = Request[IO](
    method = Method.GET,
    uri = uri,
    headers = Headers(
      Authorization(Credentials.Token(AuthScheme.Bearer, token.toString)),
      Accept(MediaType.application.json),
    ),
  )
}
