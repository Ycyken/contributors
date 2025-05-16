object domain {

  final case class Commit(author: String, message: String, time: String)

  final case class Contributor(login: String, commitCount: Long, activityTime: (String, String))

  opaque type Repos = List[String]

  object Repos {
    def apply(repos: List[String]): Repos = repos
  }

  extension (x: Repos) def value: List[String] = x

}
