package axle.quanta

import cats.kernel.Eq
import spire.algebra.Field
import spire.math.ConvertableTo
import axle.algebra.Bijection
import axle.algebra.DirectedGraph
import axle.algebra.Scale10s

case class Frequency() extends Quantum {

  def wikipediaUrl: String = "http://en.wikipedia.org/wiki/Frequency"

}

trait FrequencyUnits extends QuantumUnits[Frequency] {

  lazy val degree = unit("degree", "°", Some("http://en.wikipedia.org/wiki/Degree_(Frequency)"))
  lazy val hertz = unit("Hertz", "Hz", Some("http://en.wikipedia.org/wiki/Hertz"))
  lazy val Hz = hertz
  lazy val kilohertz = unit("Kilohertz", "KHz")
  lazy val KHz = kilohertz
  lazy val megahertz = unit("Megahertz", "MHz")
  lazy val MHz = megahertz
  lazy val gigahertz = unit("Gigahertz", "GHz")
  lazy val GHz = gigahertz

  def units: List[UnitOfMeasurement[Frequency]] =
    List(degree, hertz, kilohertz, megahertz, gigahertz)

}

trait FrequencyConverter[N] extends UnitConverter[Frequency, N] with FrequencyUnits {

  def defaultUnit = hertz
}

object Frequency {

  def converterGraphK2[N: Field: Eq: ConvertableTo, DG[_, _]](
    implicit
    evDG: DirectedGraph[DG[UnitOfMeasurement[Frequency], N => N], UnitOfMeasurement[Frequency], N => N]) =
    converterGraph[N, DG[UnitOfMeasurement[Frequency], N => N]]

  def converterGraph[N: Field: Eq: ConvertableTo, DG](
    implicit
    evDG: DirectedGraph[DG, UnitOfMeasurement[Frequency], N => N]) =
    new UnitConverterGraph[Frequency, N, DG] with FrequencyConverter[N] {

      def links: Seq[(UnitOfMeasurement[Frequency], UnitOfMeasurement[Frequency], Bijection[N, N])] =
        List[(UnitOfMeasurement[Frequency], UnitOfMeasurement[Frequency], Bijection[N, N])](
          (Hz, KHz, Scale10s(3)),
          (Hz, MHz, Scale10s(9)),
          (Hz, GHz, Scale10s(12)))

    }

}
