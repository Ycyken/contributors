import cats.effect.IO
import org.http4s.Uri

object uris {

  def uri(url: String): IO[Uri] = IO.fromEither(Uri.fromString(url))

}
