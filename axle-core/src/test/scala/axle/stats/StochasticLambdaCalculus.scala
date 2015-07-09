package axle.stats

import org.specs2.mutable.Specification

import axle.game.Dice.die
import spire.optional.unicode.Σ
import spire.implicits.IntAlgebra
import spire.math.Rational
import spire.syntax.literals._

object ProbabilityMonadSpec extends Specification {

  "iffy (stochastic if)" should {
    "map fair boolean to d6 + (d6+d6)" in {

      val distribution =
        iffy(
          binaryDecision(Rational(1, 3)),
          die(6),
          for { a <- die(6); b <- die(6) } yield a + b)

      distribution.probabilityOf(1) must be equalTo Rational(1, 18)

      distribution.probabilityOf(12) must be equalTo Rational(1, 54)

      Σ(distribution.values map distribution.probabilityOf) must be equalTo Rational(1)
    }
  }

  "π estimation" should {
    "work" in {

      import scala.math.sqrt

      val n = 200

      // TODO: sample a subset of n for each of x and y
      // to compute the distribution of estimates.
      // The naive appraoch is obviously intractable
      // for all but the smallest subsets of n.
      // However, there should be a way to utilize the "if" statement to
      // reduce the complexity.

      val piDist = for {
        x <- uniformDistribution(0 to n, "x")
        y <- uniformDistribution(0 to n, "y")
      } yield if (sqrt(x * x + y * y) <= n) 1 else 0

      1 must be equalTo 1
    }
  }

}