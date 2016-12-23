package axle.nlp

import axle.algebra.Σ
import spire.algebra.Field
import cats.kernel.Eq
import spire.implicits.multiplicativeSemigroupOps

case class UnweightedDocumentVectorSpace[D: Field]()(implicit _eqD: Eq[D])
    extends DocumentVectorSpace[D] {

  def scalar = Field[D]

  implicit def eqD = _eqD

  def dot(v1: Map[String, D], v2: Map[String, D]): D = {

    val common = (v1.keySet intersect v2.keySet).toList

    Σ(common.map(w => v1(w) * v2(w)))
  }

}
