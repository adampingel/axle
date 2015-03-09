package axle.quanta

import axle.algebra.Bijection
import axle.algebra.DirectedGraph
import spire.algebra.Eq
import spire.algebra.Field

case class Area() extends Quantum {

  def wikipediaUrl: String = "http://en.wikipedia.org/wiki/Area"

}

trait AreaMetadata[N] extends QuantumMetadata[Area, N] {

  type U = UnitOfMeasurement[Area, N]

}

object Area {

  def metadata[N] = new AreaMetadata[N] {

    def unit(name: String, symbol: String, wiki: Option[String] = None) =
      UnitOfMeasurement[Area, N](name, symbol, wiki)

    lazy val m2 = unit("m2", "m2") // derive
    lazy val km2 = unit("km2", "km2") // derive
    lazy val cm2 = unit("cm2", "cm2") // derive

    def units: List[UnitOfMeasurement[Area, N]] =
      List(m2, km2, cm2)

    def links(implicit fn: Field[N]): Seq[(UnitOfMeasurement[Area, N], UnitOfMeasurement[Area, N], Bijection[N, N])] =
      List[(UnitOfMeasurement[Area, N], UnitOfMeasurement[Area, N], Bijection[N, N])](
        (m2, km2, Scale10s(6)),
        (cm2, m2, Scale10s(6)))

  }

}