package org.specs2
package specification

import core._
import execute._
import specification.create.{Interpolated, S2StringContext, ContextualFragmentFactory, FragmentFactory, FragmentsFactory}
import data.AlwaysTag
import main.CommandLine

/**
 * For each created example use a given context
 */
trait EachContext extends FragmentsFactory:

  protected def context(env: Env): Context

  override protected def fragmentFactory: FragmentFactory =
    new ContextualFragmentFactory(super.fragmentFactory, context)

/**
 * For each created example use a context using the command line arguments
 */
trait ContextWithCommandLineArguments extends FragmentsFactory:
  protected def context(env: CommandLine): Context

  override protected def fragmentFactory: FragmentFactory =
    new ContextualFragmentFactory(super.fragmentFactory, (env: Env) => context(env.arguments.commandLine))

/**
 * For each created example use a given before action
 */
trait BeforeEach extends FragmentsFactory:
  private val outer = this

  protected def before: Any

  protected def beforeContext(env: Env): Context =
    new Before:
      def before = outer.before

  override protected def fragmentFactory: FragmentFactory =
    new ContextualFragmentFactory(super.fragmentFactory, beforeContext)

/**
 * For each created example use a given after action
 */
trait AfterEach extends FragmentsFactory:
  private val outer = this

  protected def after: Any

  protected def afterContext(env: Env): Context =
    new After:
      def after = outer.after

  override protected def fragmentFactory: FragmentFactory =
    new ContextualFragmentFactory(super.fragmentFactory, afterContext)

/**
 * For each created example use a given before action
 */
trait BeforeAfterEach extends FragmentsFactory:
  private val outer = this

  protected def before: Any
  protected def after: Any

  protected def beforeAfterContext(env: Env): Context =
    new BeforeAfter:
      def before = outer.before
      def after = outer.after

  override protected def fragmentFactory: FragmentFactory =
    new ContextualFragmentFactory(super.fragmentFactory, beforeAfterContext)

/**
 * For each created example use a given around action
 */
trait AroundEach extends FragmentsFactory:
  private val outer = this

  protected def around[R : AsResult](r: =>R): Result

  protected def aroundContext(env: Env): Context =
    new Around:
      def around[R : AsResult](r: =>R): Result = outer.around(r)

  override protected def fragmentFactory: FragmentFactory =
    new ContextualFragmentFactory(super.fragmentFactory, aroundContext)

/**
 * For each created example use a given fixture object
 */
trait ForEach[T] extends FragmentsFactory:

  protected def foreach[R : AsResult](f: T => R): Result

  protected def foreachContext(env: Env): Context =
    new Around:
      def around[R : AsResult](r: =>R) = AsResult(r)

  given [R : AsResult] as AsResult[T => R] =
    new AsResult[T => R]:
      def asResult(f: =>(T => R)) = foreach(f)

  override protected def fragmentFactory: FragmentFactory =
    new ContextualFragmentFactory(super.fragmentFactory, foreachContext)

/**
 * For each example but inject data depending on command line arguments
 */
trait ForEachWithCommandLineArguments[T] extends FragmentsFactory { outer: S2StringContext =>

  protected def foreach[R : AsResult](commandLine: CommandLine)(f: T => R): Result

  protected def foreachWithCommandLineContext(env: Env): Context =
    new Around:
      def around[R : AsResult](r: =>R) = AsResult(r)

  def foreachFunctionToExecution[R : AsResult](f: T => R): Execution =
    Execution.withEnv((env: Env) => foreach(env.arguments.commandLine)(f))

  inline def foreachFunctionIsInterpolated[R : AsResult](f: =>(T => R)): Interpolated =
    ${S2StringContext.executionInterpolated('{foreachFunctionToExecution(f)}, '{outer.fragmentFactory})}

  override protected def fragmentFactory: FragmentFactory =
    new ContextualFragmentFactory(super.fragmentFactory, foreachWithCommandLineContext)

}

/**
 * Execute some fragments before all others
 */
trait BeforeSpec extends SpecificationStructure:
  def beforeSpec: core.Fragments
  override def map(fs: =>core.Fragments) = super.map(fs).prepend(beforeSpec)

/**
 * Execute some fragments after all others
 */
trait AfterSpec extends SpecificationStructure:
  def afterSpec: core.Fragments

  override def map(fs: =>core.Fragments): core.Fragments =
    super.map(fs).append(afterSpec)

/**
 * Execute some fragments before and after all others
 */
trait BeforeAfterSpec extends SpecificationStructure:
  def beforeSpec: core.Fragments
  def afterSpec: core.Fragments
  override def map(fs: =>core.Fragments) = super.map(fs).prepend(beforeSpec).append(afterSpec)
