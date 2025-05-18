# Contributors

Application that shows information about the contributors of all repositories
of an organization on GitHub (or contributors of a specific repository),
collecting for each contributor such information as:

- number of commits
- activity time by hour
- average message size per commit

## Usage

The app is under development, for now it only works as a server.

You need to set the [config file](./src/main/resources/application.conf): specify host, port and
your [GitHub token](https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/managing-your-personal-access-tokens).

Then you can run it with [sbt](https://www.scala-sbt.org/):

```sbt
sbt run
```

The server returns JSON responses at the following endpoints:

| Method | Endpoint               | Description                               |
|--------|------------------------|-------------------------------------------|
| GET    | `/org/{org}`           | Returns contributors info of organisation |
| GET    | `/repo/{owner}/{repo}` | Returns contributors info of repository   |

## Technologies Used

- Cats Effect
- http4s
- log4cats-slf4j
- ZIO Json