import com.comcast.ip4s.{Host, Port}
import pureconfig.ConfigReader
import pureconfig.error.CannotConvert

object config {

  opaque type Token = String

  object Token {
    def apply(str: String): Token = str
    def toString(token: Token): String = token
  }

  final case class ServerConfig(host: Host, port: Port) derives ConfigReader

  given HostReader: ConfigReader[Host] =
    ConfigReader[String].emap(host => Host.fromString(host).toRight(CannotConvert(host, "Host", "incorrect host")))

  given PortReader: ConfigReader[Port] =
    ConfigReader[String].emap(port => Port.fromString(port).toRight(CannotConvert(port, "Port", "incorrect port")))

  case class AppConfig(
    token: Token,
    serverConfig: ServerConfig,
  ) derives ConfigReader

}
