package axle.algebra.distance

import org.jblas.DoubleMatrix
import org.scalacheck.Arbitrary
import org.scalatest._
import org.typelevel.discipline.Predicate
import org.typelevel.discipline.scalatest.Discipline

import spire.algebra._

import axle.algebra.LinearAlgebra
import axle.jblas.linearAlgebraDoubleMatrix
import axle.jblas.rowVectorInnerProductSpace

class CosineSpec
  extends FunSuite with Matchers
  with Discipline {

  implicit val mmInt: MultiplicativeMonoid[Int] = spire.implicits.IntAlgebra
  implicit val fieldDouble: Field[Double] = spire.implicits.DoubleAlgebra
  implicit val nrootDouble: NRoot[Double] = spire.implicits.DoubleAlgebra

  val n = 2
  implicit val innerSpace = rowVectorInnerProductSpace[Int, Int, Double](n)
  implicit val space = new Cosine[DoubleMatrix, Double]()

  implicit val pred: Predicate[Double] = new Predicate[Double] {
    def apply(a: Double) = true
  }

  implicit val arbMatrix: Arbitrary[DoubleMatrix] =
    Arbitrary(LinearAlgebra.genMatrix[DoubleMatrix, Double](1, n, 0d, 10d))

  //  checkAll(s"Cosine space on 1x${n} matrix",
  //    VectorSpaceLaws[DoubleMatrix, Double].metricSpace)

}
