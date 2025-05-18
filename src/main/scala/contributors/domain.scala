package contributors

object domain {

  final case class Commit(author: String, message: String, time: String)

  final case class Contributor(login: String, commitCount: Long, activityTime: (String, String))

  opaque type RepoName = String

  object RepoName {
    def apply(name: String): RepoName = name
  }

  extension (x: RepoName) def value: String = x

}
