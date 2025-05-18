import cats.effect.IO
import org.http4s.Uri

object uris {

  private def uriBuilder(url: String): IO[Uri] = IO.fromEither(Uri.fromString(url))

  def repos(org: String): IO[Uri] = uriBuilder(s"https://api.github.com/orgs/$org/repos")

  def repo(owner: String, repo: String): IO[Uri] = uriBuilder(s"https://api.github.com/repos/$owner/$repo")

  def commits(owner: String, repo: String, page: Int): IO[Uri] =
    uriBuilder(s"https://api.github.com/repos/$owner/$repo/commits?per_page=100&page=$page")

}
