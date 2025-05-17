import cats.effect.IO
import config._
import org.http4s.client.Client
import org.http4s.headers.{Accept, Authorization}
import org.http4s.{AuthScheme, Credentials, Headers, MediaType, Method, Request, Uri}
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.syntax.LoggerInterpolator
import zio.json._

object client {

  given logger: Logger[IO] = Main.logger

  def fetch[A](client: Client[IO], uri: Uri, default: => A)(using token: Token)(using decoder: JsonDecoder[A]): IO[A] =
    client
      .expect[String](request(uri))
      .flatMap(_.fromJson[A].fold(_ => error"return default value: $default for $uri in fetch" as default, x => IO(x)))

  private def request(uri: Uri)(using token: Token): Request[IO] = Request[IO](
    method = Method.GET,
    uri = uri,
    headers = Headers(
      Authorization(Credentials.Token(AuthScheme.Bearer, token.value)),
      Accept(MediaType.unsafeParse("application/vnd.github+json")),
    ),
  )
}
