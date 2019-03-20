import sbt._
import sbt.Keys._


// AK - This is a set of *sane(TM)* settings I use. These are mostly relevant
// during dev but some settings enable/disable settings on the language level.
object MakeScalaSafeAgain extends AutoPlugin {

  val linters = Seq(
    "adapted-args", // Warn if an argument list is modified to match the receiver.
    "by-name-right-associative", // By-name parameter of right associative operator.
    "constant", // Evaluation of a constant arithmetic expression results in an error.
    "delayedinit-select", // Selecting member of DelayedInit.
    "doc-detached", // A Scaladoc comment appears to be detached from its element.
    "inaccessible", // Warn about inaccessible types in method signatures.
    "infer-any", // Warn when a type argument is inferred to be `Any`.
    "missing-interpolator", // A string literal appears to be missing an interpolator id.
    "nullary-override", // Warn when non-nullary `def f()' overrides nullary `def f'.
    "nullary-unit", // Warn when nullary methods return Unit.
    "option-implicit", // Option.apply used implicit view.
    // "package-object-classes", // Class or object defined in package object.
    "poly-implicit-overload", // Parameterized overloaded implicit methods are not visible as view bounds.
    "private-shadow", // A private field (or class parameter) shadows a superclass field.
    "stars-align", // Pattern sequence wildcard must align with sequence component.
    "type-parameter-shadow", // A local type parameter shadows a type already in scope.
    "unsound-match" // Pattern match may not be typesafe.
  ).map(v => s"-Xlint:$v")

  val warnings = Seq(
    "dead-code", // Warn when dead code is identified.
    "extra-implicit", // Warn when more than one implicit parameter section is defined.
    "inaccessible", // Warn about inaccessible types in method signatures.
    "infer-any", // Warn when a type argument is inferred to be `Any`.
    "nullary-override", // Warn when non-nullary `def f()' overrides nullary `def f'.
    "nullary-unit", // Warn when nullary methods return Unit.
    "numeric-widen", // Warn when numerics are widened.
    "unused:implicits", // Warn if an implicit parameter is unused.
    "unused:imports", // Warn if an import selector is not referenced.
    "unused:locals", // Warn if a local definition is unused.
    "unused:params", // Warn if a value parameter is unused.
    "unused:patvars", // Warn if a variable bound in a pattern is unused.
    "unused:privates", // Warn if a private member is unused.
    "value-discard" // Warn when non-Unit expression results are unused.
  ).map(v => s"-Ywarn-$v")

  val scalacOpts = Seq(
    "-deprecation", // Emit warning and location for usages of deprecated APIs.
    "-encoding",
    "utf-8", // Specify character encoding used by source files.
    "-explaintypes", // Explain type errors in more detail.
    "-feature", // Emit warning and location for usages of features that should be imported explicitly.
    "-language:existentials", // Existential types (besides wildcard types) can be written and inferred
    "-language:experimental.macros", // Allow macro definition (besides implementation and application)
    "-language:higherKinds", // Allow higher-kinded types
    "-language:implicitConversions", // Allow definition of implicit functions called views
    "-unchecked", // Enable additional warnings where generated code depends on assumptions.
    "-Xcheckinit", // Wrap field accessors to throw an exception on uninitialized access.
    "-Xfuture", // Turn on future language features.
    "-Xmax-classfile-name", // Needed for some encrypted filesystems
    "128",
    "-Yno-adapted-args", // Do not adapt an argument list (either by inserting () or creating a tuple) to match the receiver.
    "-Ypartial-unification", // Enable partial unification in type constructor inference
    "-Yrangepos" // For scalafix
  )

  // Fail the compilation if there are any warnings.
  val FailOnWarn = "-Xfatal-warnings"

  // For traving diverging implicit expansion errors
  val LogImplicits = "-Xlog-implicits"

  override def trigger = allRequirements

  object autoImport {
    lazy val warnFail = settingKey[Boolean]("Fail on compilation warnings?")
    lazy val logImplicits = settingKey[Boolean]("Log implicit resolution")
  }
  import autoImport._

  override lazy val projectSettings = Seq(
    warnFail := true,
    logImplicits := false,
    scalacOptions ++= {
      val opts = scalacOpts ++ linters ++ warnings
      val failOrNot =
        if (warnFail.value) List(FailOnWarn) else Nil
      val logOrNot =
        if (logImplicits.value) List(LogImplicits) else Nil
      opts ++ failOrNot ++ logOrNot
    },
    scalacOptions.in(Compile, console) ~= consoleFilter,
    scalacOptions.in(Test, console) ~= consoleFilter
  )

  val consoleFilter = { options: Seq[String] =>
    options.filterNot(
      Set(
        "-Ywarn-unused:imports",
        "-Ywarn-unused-import",
        "-Ywarn-dead-code",
        "-Xfatal-warnings"
      )
    )
  }
}
