package axle.quanta

import axle.graph.DirectedGraph
import axle.graph.Vertex
import axle.algebra.Bijection
import spire.algebra.Eq
import spire.algebra.Field
import spire.implicits.DoubleAlgebra
import spire.implicits.FloatAlgebra
import spire.implicits.eqOps
import spire.implicits.moduleOps
import spire.implicits.groupOps
import spire.implicits.multiplicativeGroupOps
import spire.implicits.multiplicativeSemigroupOps
import spire.implicits.additiveGroupOps
import spire.implicits.additiveSemigroupOps
import spire.math.Rational
import spire.math.Real
import math.{ Pi => π }

abstract class Angle extends Quantum {
  def wikipediaUrl = "http://en.wikipedia.org/wiki/Degree_(angle)"
}

object Angle extends Angle {

  type Q = Angle

  def units[N: Field: Eq] = List[UnitOfMeasurement[Q, N]](
    unit("degree", "°", Some("http://en.wikipedia.org/wiki/Degree_(angle)")),
    unit("radian", "rad", Some("http://en.wikipedia.org/wiki/Radian")),
    unit("circleDegrees", "circle", Some("http://en.wikipedia.org/wiki/Circle")),
    unit("circleRadians", "circle", Some("http://en.wikipedia.org/wiki/Circle")))

  def links[N: Field: Eq] = {
    implicit val baseCG = cgnDisconnected[N]
    List[(UnitOfMeasurement[Q, N], UnitOfMeasurement[Q, N], Bijection[N, N])](
      (degree, circleDegrees, ScaleInt(360)),
      (radian, circleRadians, ScaleDouble(2 * π)),
      (circleDegrees, circleRadians, BijectiveIdentity[N]))
  }

  def degree[N: Field: Eq](implicit cg: CG[N]) = byName(cg, "degree")
  def °[N: Field: Eq](implicit cg: CG[N]) = byName(cg, "degree")
  def radian[N: Field: Eq](implicit cg: CG[N]) = byName(cg, "radian")
  def rad[N: Field: Eq](implicit cg: CG[N]) = byName(cg, "radian")
  def circleDegrees[N: Field: Eq](implicit cg: CG[N]) = byName(cg, "circleDegrees")
  def circleRadians[N: Field: Eq](implicit cg: CG[N]) = byName(cg, "circleRadians")

//  def clockwise90[N: Field: Eq] = -90 *: °[N]
//
//  def counterClockwise90[N: Field: Eq] = 90 *: °[N]

}
