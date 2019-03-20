package com.scout

import cats.effect._

import org.http4s.server.blaze._

object Main extends IOApp {
  import cats.implicits._

  // Add services here ---------------------------------------------------------

  // ---------------------------------------------------------------------------
  val router = new Router[IO]

  def run(args: List[String]): IO[ExitCode] =
    BlazeServerBuilder[IO]
      .bindHttp(8080, "localhost")
      .withHttpApp(router.api)
      .serve
      .compile
      .drain
      .as(ExitCode.Success)

}
