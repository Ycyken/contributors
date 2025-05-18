package contributors

object domain {

  final case class Commit(author: String, message: String, time: String)

  final case class Contributor(name: String, commitCount: Int, activityTime: Array[Int], avgCommitMsg: Int)

  opaque type RepoName = String

  object RepoName {
    def apply(name: String): RepoName = name
  }

  extension (x: RepoName) def value: String = x

}
