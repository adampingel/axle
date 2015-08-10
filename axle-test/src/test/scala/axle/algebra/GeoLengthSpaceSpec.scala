package axle.algebra

import org.specs2.mutable.Specification

import axle.algebra.GeoCoordinates.geoCoordinatesLengthSpace
import axle.algebra.modules.doubleDoubleModule
import axle.algebra.modules.doubleRationalModule
import axle.distanceOnSphere
import axle.jung.directedGraphJung
import axle.quanta.Angle
import axle.quanta.Distance
import axle.quanta.UnitOfMeasurement
import axle.quanta.UnittedQuantity
import edu.uci.ics.jung.graph.DirectedSparseGraph
import spire.implicits.DoubleAlgebra
import spire.implicits.metricSpaceOps
import spire.implicits.moduleOps

class GeoLengthSpaceSpec extends Specification {

  implicit val angleConverter = Angle.converterGraph[Double, DirectedSparseGraph[UnitOfMeasurement[Angle], Double => Double]]
  import angleConverter.°
  import angleConverter.radian

  implicit val distanceConverter = Distance.converterGraph[Double, DirectedSparseGraph[UnitOfMeasurement[Distance], Double => Double]]
  import axle.quanta.UnittedQuantity

  val sf = GeoCoordinates(37.7833 *: °, 122.4167 *: °)
  val ny = GeoCoordinates(40.7127 *: °, 74.0059 *: °)
  val lax = GeoCoordinates(0.592539 *: radian, 2.066470 *: radian)
  val jfk = GeoCoordinates(0.709186 *: radian, 1.287762 *: radian)

  "geo metric space" should {

    "calculate distance from San Francisco to New York" in {

      val degreesDistance = ((sf distance ny) in °).magnitude

      import distanceConverter.km
      val earthRadius = 6371d *: km
      val kmDistance = (distanceOnSphere(sf distance ny, earthRadius) in km).magnitude

      degreesDistance must be equalTo 37.12896941431725
      kmDistance must be equalTo 4128.553030413071
    }

    "calculate angular distance from LAX to JFK" in {

      ((lax distance jfk) in radian).magnitude must be equalTo 0.6235849243922914
    }
  }

  // See http://williams.best.vwh.net/avform.htm

  "geo length space" should {
    "calculate the way-point 40% from LAX to JFK correctly" in {

      import axle.algebra.GeoCoordinates.geoCoordinatesLengthSpace

      val waypoint = geoCoordinatesLengthSpace.onPath(lax, jfk, 0.4)

      (waypoint.latitude in °).magnitude must be equalTo 38.66945192546367
      (waypoint.longitude in °).magnitude must be equalTo 101.6261713931811
    }
  }

}
