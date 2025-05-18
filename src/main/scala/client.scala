import cats.effect.IO
import cats.implicits._
import cats.syntax.all._
import config._
import domain.Commit
import org.http4s._
import org.http4s.client.Client
import org.http4s.headers.{Accept, Authorization, Link}
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.syntax.LoggerInterpolator
import zio.json._

object client {

  given logger: Logger[IO] = Main.logger

  final case class SimpleResponse(status: Status, headers: Headers, body: String)

  def fetch(client: Client[IO], uri: Uri)(using token: Token): IO[SimpleResponse] =
    IO.defer(
      client
        .run(request(uri))
        .use(r =>
          for {
            body <- r.as[String]
          } yield SimpleResponse(r.status, r.headers, body),
        ),
    )

  private def fetchBody[A](client: Client[IO], uri: Uri, default: => A)(using
    token: Token,
  )(using decoder: JsonDecoder[A]): IO[A] =
    client
      .expect[String](request(uri))
      .flatMap(
        _.fromJson[A]
          .fold(_ => error"return default value: $default for '$uri' request in fetch" as default, x => IO(x)),
      )

  def fetchRepoCommits(client: Client[IO], owner: String, repo: String)(using token: Token): IO[List[Commit]] = {
    given commitsDecoder: JsonDecoder[List[Commit]] = serde.decodeCommits

    for {
      _ <- info"start fetch repo $owner/$repo commits"
      firstPageUri <- uris.commits(owner, repo, 1)
      firstResponse <- fetch(client, firstPageUri)
      firstPageCommits <- firstResponse.body.fromJson[List[Commit]] match {
        case Right(d) => IO(d)
        case Left(s) => IO.raiseError(RuntimeException(s"can't decode $s"))
      }
      _ <- info"fetched $owner/$repo commits first page"
      lastPage <-
        firstResponse.headers.get[Link].map(extractLastPageNumber).fold(IO(1))(identity)
      _ <- info"get $owner/$repo commits last page: $lastPage"
      pages = (2 to lastPage).toList

      otherCommits <- pages.parUnorderedFlatTraverse(page =>
        for {
          _ <- info"start $owner/$repo commits fetch on page $page"
          pageUri <- uris.commits(owner, repo, page)
          anotherCommits <- fetchBody(client, pageUri, List.empty)
          _ <- info"fetched $owner/$repo ${anotherCommits.length} commits on page $page"
        } yield anotherCommits,
      )
    } yield firstPageCommits ++ otherCommits
  }

  private def extractLastPageNumber(link: Link): IO[Int] =
    for {
      _ <- info"get link: $link"
      linkValues <- IO.pure(link.values)
      _ <- info"get link values: $linkValues"
      lastRel <- IO.fromOption(linkValues.find(_.rel.contains("last")))(
        RuntimeException(s"can't find 'last' relation in link header $link"),
      )
      _ <- info"get 'last' relation in link header: $lastRel"
      lastPage <- IO.fromOption(lastRel.uri.query.params.get("page").map(_.toInt))(
        RuntimeException(s"can't get 'page' parameter in last relation $lastRel"),
      )
    } yield lastPage

  private def request(uri: Uri)(using token: Token): Request[IO] = Request[IO](
    method = Method.GET,
    uri = uri,
    headers = Headers(
      Authorization(Credentials.Token(AuthScheme.Bearer, token.value)),
      Accept(MediaType.unsafeParse("application/vnd.github+json")),
    ),
  )
}
