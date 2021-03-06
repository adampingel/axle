package axle.nlp

import axle.algebra.Finite
import axle.algebra.Indexed
import axle.algebra.LinearAlgebra
import axle.syntax.finite.finiteOps
import axle.syntax.indexed.indexedOps
import axle.syntax.linearalgebra.matrixOps
import spire.algebra.MetricSpace
import spire.algebra.Ring
import spire.implicits.additiveGroupOps
import spire.implicits.additiveSemigroupOps
import cats.kernel.Eq
import cats.kernel.Order
import cats.implicits._
import cats.Order.catsKernelOrderingForOrder

/**
 * Based on the Scala implementation of
 *
 * http://en.wikibooks.org/wiki/Algorithm_Implementation/Strings/Levenshtein_distance
 *
 */

case class Levenshtein[S[_], E: Eq, M, I: Ring: Order]()(
  implicit
  la:     LinearAlgebra[M, I, I, I],
  idx:    Indexed[S, I],
  finite: Finite[S, I])
  extends MetricSpace[S[E], I] {

  def distance(s1: S[E], s2: S[E]): I = {

    val i0 = Ring[I].zero
    val i1 = Ring[I].one

    val lenStr1: I = s1.size
    val lenStr2: I = s2.size

    val d = la.matrix(
      lenStr1 + i1,
      lenStr2 + i1,
      i0,
      (r: I) => r,
      (c: I) => c,
      (r: I, c: I, diag: I, left: I, top: I) =>
        min(
          left + i1,
          top + i1,
          diag + (if (s1.at(r - i1) === s2.at(c - i1)) i0 else i1)))

    d.get(lenStr1, lenStr2)
  }

  def min(nums: I*): I = nums.min

}
