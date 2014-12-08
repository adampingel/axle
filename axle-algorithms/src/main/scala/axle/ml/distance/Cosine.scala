package axle.ml.distance

import axle.algebra.Matrix
import axle.syntax.matrix.matrixOps
import spire.algebra.Field
import spire.algebra.InnerProductSpace
import spire.implicits.DoubleAlgebra

/**
 *
 * Cosine space
 *
 * @param n = num columns in row vectors
 *
 * distance(r1, r2) = 1.0 - abs(rvDot(r1, r2) / (norm(r1) * norm(r2)))
 *
 * TODO: distance calcs could assert(r1.isRowVector && r2.isRowVector && r1.length === r2.length)
 *
 */

case class Cosine[M[_]](n: Int)(implicit ev: Matrix[M]) extends InnerProductSpace[M[Double], Double] {

  def negate(x: M[Double]): M[Double] = x.negate

  def zero: M[Double] = ev.zeros[Double](1, n)

  def plus(x: M[Double], y: M[Double]): M[Double] = x + y

  def timesl(r: Double, v: M[Double]): M[Double] = v * r

  def scalar: Field[Double] = DoubleAlgebra

  def dot(v: M[Double], w: M[Double]): Double = v.mulPointwise(w).rowSums.scalar

}