
package axle.pgm

import org.specs2.mutable._

import axle._
import axle.algebra.DirectedGraph
import axle.algebra.UndirectedGraph
import axle.stats._
import axle.pgm._
import spire.implicits._
import spire.math._

import axle.jung.undirectedGraphJung
import axle.jung.directedGraphJung
import edu.uci.ics.jung.graph.DirectedSparseGraph
import edu.uci.ics.jung.graph.UndirectedSparseGraph

class ScalaFigures extends Specification {

  val bools = Vector(true, false)

  def ubd(name: String) = UnknownDistribution0[Boolean, Rational](bools, name)

  val A = ubd("A")
  val B = ubd("B")
  val C = ubd("C")
  val D = ubd("D")
  val E = ubd("E")

  def figure6_1: BayesianNetwork[Boolean, Rational, DirectedSparseGraph] = {

    val aFactor = Factor(Vector(A), Map(
      Vector(A is true) -> Rational(6, 10),
      Vector(A is false) -> Rational(4, 10)))

    val bFactor = Factor(Vector(B), Map(
      Vector(B is true, A is true) -> Rational(2, 10),
      Vector(B is true, A is false) -> Rational(8, 10),
      Vector(B is false, A is true) -> Rational(3, 4),
      Vector(B is false, A is false) -> Rational(1, 4)))

    val cFactor = Factor(Vector(C), Map(
      Vector(C is true, A is true) -> Rational(4, 5),
      Vector(C is true, A is false) -> Rational(1, 5),
      Vector(C is false, A is true) -> Rational(1, 10),
      Vector(C is false, A is false) -> Rational(9, 10)))

    val dFactor = Factor(Vector(D), Map(
      Vector(D is true, B is true, C is true) -> Rational(19, 20),
      Vector(D is true, B is true, C is false) -> Rational(1, 20),
      Vector(D is true, B is false, C is true) -> Rational(9, 10),
      Vector(D is true, B is false, C is false) -> Rational(1, 10),
      Vector(D is false, B is true, C is true) -> Rational(4, 5),
      Vector(D is false, B is true, C is false) -> Rational(1, 5),
      Vector(D is false, B is false, C is true) -> Rational(0, 1),
      Vector(D is false, B is false, C is false) -> Rational(1, 1)))

    val eFactor = Factor(Vector(E), Map(
      Vector(E is true, C is true) -> Rational(7, 10),
      Vector(E is true, C is false) -> Rational(3, 10),
      Vector(E is false, C is true) -> Rational(0),
      Vector(E is false, C is false) -> Rational(1)))

    // edges: ab, ac, bd, cd, ce
    BayesianNetwork[Boolean, Rational, DirectedSparseGraph](
      "6.1",
      Map(A -> aFactor,
        B -> bFactor,
        C -> cFactor,
        D -> dFactor,
        E -> eFactor))
  }

  def figure6_2: Factor[Boolean, Rational] = figure6_1.jointProbabilityTable

  def figure6_3: (Factor[Boolean, Rational], Factor[Boolean, Rational]) = {

    //Figure 3.1
    val cptB = Factor(Vector(B, C, D), Map(
      Vector(B is true, C is true, D is true) -> Rational(95, 100),
      Vector(B is true, C is true, D is false) -> Rational(5, 100),
      Vector(B is true, C is false, D is true) -> Rational(9, 10),
      Vector(B is true, C is false, D is false) -> Rational(1, 10),
      Vector(B is false, C is true, D is true) -> Rational(8, 10),
      Vector(B is false, C is true, D is false) -> Rational(2, 10),
      Vector(B is false, C is false, D is true) -> Rational(0),
      Vector(B is false, C is false, D is false) -> Rational(1)))

    // Figure 3.2
    val cptD = Factor(Vector(D, E), Map(
      Vector(D is true, E is true) -> Rational(448, 1000),
      Vector(D is true, E is false) -> Rational(192, 1000),
      Vector(D is false, E is true) -> Rational(112, 1000),
      Vector(D is false, E is false) -> Rational(248, 1000)))

    val h = (cptB.sumOut(D)).sumOut(C)
    val m = cptB * cptD

    (cptB, cptD)
  }

  def figure6_4: BayesianNetwork[Boolean, Rational, DirectedSparseGraph] = {

    val aFactor = Factor(Vector(A), Map(
      Vector(A is true) -> Rational(6, 10),
      Vector(A is false) -> Rational(4, 10)))

    val bFactor = Factor(Vector(B), Map( // B | A
      Vector(B is true, A is true) -> Rational(9, 10),
      Vector(B is true, A is false) -> Rational(1, 10),
      Vector(B is false, A is true) -> Rational(2, 10),
      Vector(B is false, A is false) -> Rational(8, 10)))

    val cFactor = Factor(Vector(C), Map( // C | B
      Vector(C is true, B is true) -> Rational(3, 10),
      Vector(C is true, B is false) -> Rational(7, 10),
      Vector(C is false, B is true) -> Rational(1, 2),
      Vector(C is false, B is false) -> Rational(1, 2)))

    // edges: ab, bc
    val bn = BayesianNetwork[Boolean, Rational, DirectedSparseGraph]("6.4",
      Map(A -> aFactor,
        B -> bFactor,
        C -> cFactor))

    val pB = (((bn.cpt(B) * bn.cpt(A)).sumOut(A)) * bn.cpt(C)).sumOut(C)

    bn
  }

  def figure6_5: List[InteractionGraph[Boolean, Rational, UndirectedSparseGraph]] =
    figure6_1.interactionGraph.eliminationSequence(List(B, C, A, D))

  def figure6_7 = {

    val f61 = figure6_1

    // Figure 6.1 pruned towards B & E
    val Q1: Set[Distribution[Boolean, Rational]] = Set(B, E)
    val f67pBE = f61.pruneNetworkVarsAndEdges(Q1, None)

    // Figure 6.2 pruned towards B
    val Q2: Set[Distribution[Boolean, Rational]] = Set(B)
    val f67pB = f61.pruneNetworkVarsAndEdges(Q2, None)

    (f67pBE, f67pB)
  }

  // Figure 6.1 with edges pruned towards C=false
  def figure6_8 = figure6_1.pruneEdges("Figure 6.8", Some(List(C is false)))

  // Figure 6.1 pruned towards Q={D} and A=true,C=false
  def figure6_9 =
    figure6_1.pruneNetworkVarsAndEdges(Set(D), Some(List(A is true, C is false)))

  // Result of fe-i on a->b->c with Q={C}
  def figure7_2 = figure6_4.factorElimination1(Set(C))

  def figure7_4: (BayesianNetwork[Boolean, Rational, DirectedSparseGraph], EliminationTree[Boolean, Rational, UndirectedSparseGraph], Factor[Boolean, Rational]) = {

    val f61 = figure6_1

    val τ = {
      val a = f61.cpt(A)
      val b = f61.cpt(B)
      val c = f61.cpt(C)
      val d = f61.cpt(D)
      val e = f61.cpt(E)
      EliminationTree[Boolean, Rational, UndirectedSparseGraph](
        Vector(a, b, c, d, e),
        List((a, b), (a, d), (d, c), (c, e)))
    }

    // factorElimination2 on figure6.1 with Q={C} and τ={...} and r=n3
    val (f68, elim) = f61.factorElimination2(Set(C), τ, f61.cpt(C))
    (f68, τ, f61.cpt(C))
  }

  // factorElimination3 on figure6.1 with Q={C} and τ={...} and r=n3
  def figure7_5 = {
    // TODO: needs to be immutable
    val f61 = figure6_1
    val (bn, τ, cptC) = figure7_4
    val (f75, elim) = f61.factorElimination2(Set(C), τ, cptC)
    f75
  }

  def figure7_12 = {

    val abc: Set[Distribution[Boolean, Rational]] = Set(A, B, C)
    val bcd: Set[Distribution[Boolean, Rational]] = Set(B, C, D)
    val ce: Set[Distribution[Boolean, Rational]] = Set(C, E)

    JoinTree.makeJoinTree(
      Vector(abc, bcd, ce),
      List((abc, bcd), (bcd, ce)))
  }

  "bayesian networks" should {
    "work" in {

      1 must be equalTo 1
    }
  }

}
