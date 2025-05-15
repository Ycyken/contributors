import cats.effect.IO
import org.http4s.Uri

object uris {

  def uri(url: String): IO[Uri] = IO.fromEither(Uri.fromString(url))

  def repos(org: String): String = s"https://api.github.com/orgs/$org/repos"

  def repo(owner: String, repo: String): String = s"https://api.github.com/repos/$owner/$repo"

  def commits(owner: String, repo: String, page: Int): String =
    s"https://api.github.com/repos/$owner/$repo/commits?per_page=100&page=$page"

}
