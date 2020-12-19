package org.specs2
package matcher

import text.Sentences._
import execute._, Result._

/**
 * This trait can be used in conjunction with Pattern matchers:
 *
 * List(1, 2) must beLike { case List(a, b) => ok }
 * List(1, 2) must beLike { case List(a, b) => ko("unexpected") }
 */
trait ExpectedResults extends ExpectationsCreation:
  def ok(m: String): Result =
    checkResultFailure(result(true, m))

  def ko(m: String): Result =
    checkResultFailure(result(false, negateSentence(m)))

  lazy val ok: Result =
    checkResultFailure(result(true, "ko"))

  lazy val ko: Result =
    checkResultFailure(result(false, "ko"))

object ExpectedResults extends ExpectedResults
