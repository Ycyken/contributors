import cats.effect.{IO, IOApp}
import client.fetchRepoCommits
import config._
import domain.Commit
import org.http4s.HttpRoutes
import org.http4s.Method.GET
import org.http4s.client.Client
import org.http4s.dsl.io._
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.ember.server._
import org.http4s.implicits._
import org.typelevel.log4cats.slf4j.Slf4jFactory
import org.typelevel.log4cats.syntax.LoggerInterpolator
import org.typelevel.log4cats.{Logger, LoggerFactory}
import pureconfig.ConfigSource
import zio.json.{EncoderOps, JsonEncoder}

object Main extends IOApp.Simple {

  given loggerFactory: LoggerFactory[IO] = Slf4jFactory.create[IO]

  given logger: Logger[IO] = loggerFactory.getLogger

  def run: IO[Unit] = (for {
    config <- IO.delay(ConfigSource.default.loadOrThrow[AppConfig]).toResource
    client <- EmberClientBuilder.default[IO].build
    _ <- server(client, config)
  } yield ()).useForever

  private def routes(client: Client[IO], token: Token) = {
    given tk: Token = token

    given commitsEncoder: JsonEncoder[List[Commit]] = serde.encodeCommits

    HttpRoutes
      .of[IO] {
        case GET -> Root / "repo" / owner / repo =>
          info"get repo request on owner: $owner, repo: $repo"
          val res: IO[String] = for {
            commitList: List[Commit] <- fetchRepoCommits(client, owner, repo)
              .onError(e => error"can't fetch repo commits: $e")
            _ <- info"return ${commitList.length} commits in response at '/repo/$owner/$repo'"
            serializedCommits = commitList.toJson
          } yield serializedCommits
          Ok(res)
        case GET -> Root / "org" / _ => Ok("organisation repositories not implemented")
      }
      .orNotFound
  }

  private def server(client: Client[IO], config: AppConfig) = EmberServerBuilder
    .default[IO]
    .withHost(config.serverConfig.host)
    .withPort(config.serverConfig.port)
    .withHttpApp(routes(client, config.token))
    .build

}
