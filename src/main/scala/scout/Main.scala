package com.scout

import cats.effect._
import org.http4s.server.blaze._
import com.scout.repo.CarRepo

object Main extends IOApp {
  import cats.implicits._

  val router = new Router[IO](CarRepo.dummyInterpreter[IO](Map.empty))

  def run(args: List[String]): IO[ExitCode] =
    BlazeServerBuilder[IO]
      .bindHttp(8080, "localhost")
      .withHttpApp(router.api)
      .serve
      .compile
      .drain
      .as(ExitCode.Success)

}
