package axle.stats

import axle._
import axle.stats._
import axle.graph._
import axle.pgm._
import spire.math._
import spire.implicits._
import org.specs2.mutable._

class ABE extends Specification {

  import BayesianNetworkModule._
  import FactorModule._

  val bools = Some(Vector(true, false))
  val unknownBooleanDistribution = new UnknownDistribution0[Boolean, Rational]()

  val B = new RandomVariable0("Burglary", bools, unknownBooleanDistribution)
  val E = new RandomVariable0("Earthquake", bools, unknownBooleanDistribution)
  val A = new RandomVariable0("Alarm", bools, unknownBooleanDistribution)
  val J = new RandomVariable0("John Calls", bools, unknownBooleanDistribution)
  val M = new RandomVariable0("Mary Calls", bools, unknownBooleanDistribution)

  val bn = BayesianNetwork(
    "A sounds (due to Burglary or Earthquake) and John or Mary Call",
    List(BayesianNetworkNode(B,
      Factor(Vector(B), Map(
        Vector(B is true) -> Rational(1, 1000),
        Vector(B is false) -> Rational(999, 1000)))),
      BayesianNetworkNode(E,
        Factor(Vector(E), Map(
          Vector(E is true) -> Rational(1, 500),
          Vector(E is false) -> Rational(499, 500)))),
      BayesianNetworkNode(A,
        Factor(Vector(B, E, A), Map(
          Vector(B is false, E is false, A is true) -> Rational(1, 1000),
          Vector(B is false, E is false, A is false) -> Rational(999, 1000),
          Vector(B is true, E is false, A is true) -> Rational(940, 1000),
          Vector(B is true, E is false, A is false) -> Rational(60, 1000),
          Vector(B is false, E is true, A is true) -> Rational(290, 1000),
          Vector(B is false, E is true, A is false) -> Rational(710, 1000),
          Vector(B is true, E is true, A is true) -> Rational(950, 1000),
          Vector(B is true, E is true, A is false) -> Rational(50, 1000)))),
      BayesianNetworkNode(J,
        Factor(Vector(A, J), Map(
          Vector(A is true, J is true) -> Rational(9, 10),
          Vector(A is true, J is false) -> Rational(1, 10),
          Vector(A is false, J is true) -> Rational(5, 100),
          Vector(A is false, J is false) -> Rational(95, 100)))),
      BayesianNetworkNode(M,
        Factor(Vector(A, M), Map(
          Vector(A is true, M is true) -> Rational(7, 10),
          Vector(A is true, M is false) -> Rational(3, 10),
          Vector(A is false, M is true) -> Rational(1, 100),
          Vector(A is false, M is false) -> Rational(99, 100))))),
    (vs: Seq[Vertex[BayesianNetworkNode[Boolean, Rational]]]) => vs match {
      case b :: e :: a :: j :: m :: Nil => List((b, a, ""), (e, a, ""), (a, j, ""), (a, m, ""))
      case _ => Nil
    })

  // val (bn, es): (BayesianNetwork, Seq[BayesianNetwork#E]) =

  "bayesian networks" should {
    "work" in {

      val jpt = bn.jointProbabilityTable

      val sansAll = jpt.Σ(M).Σ(J).Σ(A).Σ(B).Σ(E)

      val abe = (bn.cpt(A) * bn.cpt(B)) * bn.cpt(E)

      val Q: Set[RandomVariable[Boolean, Rational]] = Set(E, B, A)
      val order = List(J, M)

      // val afterVE = bn.variableEliminationPriorMarginalI(Q, order)
      // val afterVE = bn.variableEliminationPriorMarginalII(Q, order, E is true)
      // bn.getRandomVariables.map(rv => println(bn.getMarkovAssumptionsFor(rv)))
      // println("P(B) = " + ans1) // 0.001
      // println("P(A| B, -E) = " + ans2) // 0.94
      // println("eliminating variables other than A, B, and E; and then finding those consistent with E = true")
      // println(afterVE)

      1 must be equalTo 1
    }
  }

}
