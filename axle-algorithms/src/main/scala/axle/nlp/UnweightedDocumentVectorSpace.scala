package axle.nlp

import scala.reflect.ClassTag

import axle.algebra.Σ
import spire.algebra.Field
import spire.implicits.multiplicativeSemigroupOps

case class UnweightedDocumentVectorSpace[D: Field: ClassTag]()
    extends DocumentVectorSpace[D] {

  def scalar = Field[D]

  def dot(v1: Map[String, D], v2: Map[String, D]): D = {

    val common = (v1.keySet intersect v2.keySet).toList

    Σ(common.map(w => v1(w) * v2(w)))
  }

}

