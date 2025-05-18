package contributors

import cats.effect.{IO, IOApp}
import contributors.client.{fetchOrgCommits, fetchRepoCommits}
import contributors.config._
import contributors.domain.{Commit, Contributor}
import contributors.service.processCommits
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

    given contributorsEncoder: JsonEncoder[List[Contributor]] = serde.encodeContributors

    HttpRoutes
      .of[IO] {
        case GET -> Root / "repo" / owner / repo =>
          info"get repo request on owner: $owner, repo: $repo"
          Ok(for {
            commits: List[Commit] <- fetchRepoCommits(client, owner, repo)
              .onError(e => error"can't fetch commits from repo $repo: $e")

            _ <- info"start commits processing"
            contributors <- IO(processCommits(commits))
            serializedContributors = contributors.toJson
          } yield serializedContributors)

        case GET -> Root / "org" / org =>
          info"get repo request on org: $org"

          Ok(for {
            commits: List[Commit] <- fetchOrgCommits(client, org)
              .onError(e => error"can't fetch commits from org $org: $e")

            _ <- info"start commits processing"
            contributors <- IO(processCommits(commits))
            serializedContributors = contributors.toJson
          } yield serializedContributors)
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
