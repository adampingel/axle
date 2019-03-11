package axle

import org.scalatest._

import edu.uci.ics.jung.graph.DirectedSparseGraph

import cats.implicits._

import spire.math.Rational
import spire.algebra._

import axle.stats.ConditionalProbabilityTable0
import axle.stats.ProbabilityModel
import axle.stats.Variable
import axle.stats.coin
import axle.stats.entropy
import axle.quanta.Information
import axle.jung.directedGraphJung

class InformationTheorySpec extends FunSuite with Matchers {

  implicit val monad = ProbabilityModel.monad[({ type λ[T] = ConditionalProbabilityTable0[T, Rational] })#λ, Rational]
  implicit val prob = implicitly[ProbabilityModel[({ type λ[T] = ConditionalProbabilityTable0[T, Rational] })#λ, Rational]]

  implicit val fieldDouble: Field[Double] = spire.implicits.DoubleAlgebra

  test("hard-coded distributions") {

    implicit val id = Information.converterGraphK2[Double, DirectedSparseGraph]

    val d =
      ConditionalProbabilityTable0(Map(
        "A" -> Rational(2, 10),
        "B" -> Rational(1, 10),
        "C" -> Rational(7, 10)), Variable[String]("d"))

    val e = entropy[({ type λ[T] = ConditionalProbabilityTable0[T, Rational] })#λ, String, Rational](d)

    e.magnitude should ===(1.1567796494470395)
  }

  /*
  test("cpt") {

    val X = ConditionalProbabilityTable0(Map(
      "foo" -> Rational(1, 10),
      "food" -> Rational(9, 10)), Variable[String]("X"))

    val Y = ConditionalProbabilityTable0(Map(
      "bar" -> Rational(9, 10),
      "bard" -> Rational(1, 10)), Variable[String]("Y"))

    // Note: A is given X and Y
    val A = ConditionalProbabilityTable2(Map(
      ("foo", "bar") -> Map("a" -> Rational(3, 10), "b" -> Rational(7, 10)),
      ("foo", "bard") -> Map("a" -> Rational(2, 10), "b" -> Rational(8, 10)),
      ("food", "bar") -> Map("a" -> Rational(9, 10), "b" -> Rational(1, 10)),
      ("food", "bard") -> Map("a" -> Rational(5, 10), "b" -> Rational(5, 10))),
      Variable[String]("A"))

    //val p = P((A is "a") | (X is "foo") ∧ (Y isnt "bar"))
    //val b = P((A is "a") ∧ (X is "foo")).bayes

    // TODO
    1 should be(1)
  }
  */

  test("coins") {

    val biasedCoin = coin(Rational(9, 10))
    val fairCoin = coin()

    implicit val id = Information.converterGraphK2[Double, DirectedSparseGraph]

    // assumes entropy is in bits
    val biasedCoinEntropy = entropy[({ type λ[T] = ConditionalProbabilityTable0[T, Rational] })#λ, Symbol, Rational](biasedCoin)
    biasedCoinEntropy.magnitude should be(0.4689955935892812)

    val fairCoinEntropy = entropy[({ type λ[T] = ConditionalProbabilityTable0[T, Rational] })#λ, Symbol, Rational](fairCoin)
    fairCoinEntropy.magnitude should be(1d)
  }

}
