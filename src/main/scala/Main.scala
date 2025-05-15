import cats.effect.{IO, IOApp}
import com.comcast.ip4s.{host, port}
import config._
import org.http4s.HttpRoutes
import org.http4s.Method.GET
import org.http4s.dsl.io._
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.ember.server._
import org.http4s.implicits._
import org.typelevel.log4cats.slf4j.Slf4jFactory
import org.typelevel.log4cats.{Logger, LoggerFactory}
import pureconfig.ConfigSource

object Main extends IOApp.Simple {

  given loggerFactory: LoggerFactory[IO] = Slf4jFactory.create[IO]
  given logger: Logger[IO] = loggerFactory.getLogger

  def run: IO[Unit] = (for {
    _ <- IO.delay(ConfigSource.default.loadOrThrow[AppConfig]).toResource
    _ <- client
    _ <- server
  } yield ()).useForever

  private def routes = HttpRoutes
    .of[IO] { case GET -> Root => Ok("Server started") }
    .orNotFound

  private val client = EmberClientBuilder.default[IO].build

  private val server = EmberServerBuilder
    .default[IO]
    .withHost(host"localhost")
    .withPort(port"8080")
    .withHttpApp(routes)
    .build

}
