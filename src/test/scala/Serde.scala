import contributors.domain.{Commit, RepoName}
import zio.json.{DecoderOps, EncoderOps, JsonDecoder, JsonEncoder}

import scala.io.Source
import scala.util.Using

class Serde extends munit.FunSuite {

  def readJsonResource(filename: String): String =
    Using.resource(getClass.getResourceAsStream(filename)) { stream =>
      Source.fromInputStream(stream).mkString
    }

  test("decode organisation repo names from response is correct") {
    val jsonString = readJsonResource("repos-response.json")
    given decoder: JsonDecoder[List[RepoName]] = contributors.serde.decodeRepoNames

    val actual = jsonString.fromJson.fold(
      err => fail(s"Can't decode rust repositories: $err"),
      identity,
    )
    val expected = List("rust", "prev.rust-lang.org", "rust-playpen", "rust-enhanced", "llvm").map(RepoName(_))
    assertEquals(expected, actual)
  }

  test("decode commits response is correct") {
    val jsonString = readJsonResource("commits-response.json")

    given decoder: JsonDecoder[List[Commit]] = contributors.serde.decodeCommits

    val actual = jsonString.fromJson.fold(
      err => fail(s"Can't decode rust commits: $err"),
      identity,
    )
    val expected =
      List(
        Commit("daniel-white", "Fix macos flags to u64", "2025-05-20T19:33:43Z"),
        Commit(
          "cberner",
          "Make CI more strict",
          "2025-05-20T20:25:17Z",
        ),
        Commit(
          "allisonkarlitskaya",
          "tests: add a passthrough test\n\n...and run it from CI via `make test_passthrough`.\n\nWe need to upgrade to ubuntu-24.04 in order to do this because we need a\nkernel with passthrough support.",
          "2025-05-06T15:31:30Z",
        ),
        Commit(
          "allisonkarlitskaya",
          "examples: add an example for passthrough fds\n\nThis is copied from the same template as the other examples.  It\nincludes an example of how a filesystem might choose to cache and reuse\nBackingIds.",
          "2025-05-01T19:02:25Z",
        ),
      )
    assertEquals(expected, actual)
  }
}
