package org.specs2
package matcher

import execute._, Result._
import text.Regexes._
import text.Trim._

/**
 * Matchers for checking if a piece of code compiles or not
 */
trait TypecheckMatchers:
  def succeed: Matcher[Typechecked] =
    new TypecheckMatcher

  def failWith(message: String): Matcher[Typechecked] =
    FailTypecheckMatcher(message)

object TypecheckMatchers extends TypecheckMatchers

class TypecheckMatcher extends Matcher[Typechecked]:
  def apply[S <: Typechecked](actual: Expectable[S]): Result =
    result(actual.value.isSuccess, message(actual.value.result))

  private def message(r: TypecheckResult): String =
    r match
      case TypecheckSuccess            => "typecheck error"
      case CanTypecheckLiteralsOnly    => "only literals can be typechecked"
      case TypecheckError(m)           => "typecheck error: "+m
      case ParseError(m)               => "parse error: "+m
      case UnexpectedTypecheckError(m) => "unexpected error: "+m

case class FailTypecheckMatcher(expected: String) extends Matcher[Typechecked]:
  def apply[S <: Typechecked](actual: Expectable[S]): Result =
    result(!actual.value.isSuccess && resultMessage(actual.value.result)
      .map(_.removeAll("\n").removeAll("\r")).exists(_ matchesSafely ".*"+expected+".*"),
      message(actual.value.result, expected))

  private def resultMessage(r: TypecheckResult): Option[String] =
    r match
      case TypecheckSuccess            => None
      case CanTypecheckLiteralsOnly    => None
      case TypecheckError(m)           => Some(m)
      case ParseError(m)               => Some(m)
      case UnexpectedTypecheckError(m) => Some(m)

  private def message(r: TypecheckResult, expected: String): String =
    r match
      case TypecheckSuccess            => "the code typechecks ok"
      case CanTypecheckLiteralsOnly    => "only literals can be typechecked"
      case TypecheckError(m)           => s"$m\n doesn't match\n$expected"
      case ParseError(m)               => s"$m\n doesn't match\n$expected"
      case UnexpectedTypecheckError(m) => s"$m\n doesn't match\n$expected"
