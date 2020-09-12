package axle

import cats.kernel.Eq

import spire.math.Rational
import spire.random.Dist

package object probability {

  // type CPTR[A] = ConditionalProbabilityTable[A, Rational]

  val rationalProbabilityDist: Dist[Rational] = {
    val denominator = Integer.MAX_VALUE - 1 // 1000000
    val x = (i: Int) => Rational(i.toLong, denominator.toLong)
    val y = Dist.intrange(0, denominator)
    Dist(x)(y)
  }

  def bernoulliDistribution(pOne: Rational): ConditionalProbabilityTable[Int, Rational] =
    ConditionalProbabilityTable[Int, Rational](
      Map(
        1 -> pOne,
        0 -> (1 - pOne)))

  def binaryDecision(yes: Rational): ConditionalProbabilityTable[Boolean, Rational] =
    ConditionalProbabilityTable(Map(true -> yes, false -> (1 - yes)))

  def uniformDistribution[T: Eq](values: Seq[T]): ConditionalProbabilityTable[T, Rational] = {

    val grouped = values.groupBy(identity)
    val dist: Map[T, Rational] = grouped.map({ kvs =>
       val rk = kvs._1
       val v = Rational(kvs._2.size.toLong, values.size.toLong)
       rk -> v
    })

    ConditionalProbabilityTable(dist)
  }

}
