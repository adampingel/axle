package axle.slc

import org.specs2.mutable.Specification

class Traffic extends Specification {

  "traffic" should {
    "work" in {

      import spire.implicits.DoubleAlgebra

      import axle.slc._

      val light1 = Dist[Double, String](
        0.45 -> "Red", 0.1 -> "Yellow", 0.45 -> "Green")

      val cautiousDriver =
        Case("Red", Dist(0.2 -> "Braking", 0.8 -> "Stopped"),
          Case("Yellow", Dist(0.9 -> "Braking", 0.1 -> "Driving"),
            Case("Green" -> "Driving")

      val aggressiveDriver = Dist[Double, String](
        Map("Red" -> Map("Braking" -> 0.3, "Stopped" -> 0.6, "Driving" -> 0.1),
          "Yellow" -> Map("Braking" -> 0.1, "Driving" -> 0.9),
          "Green" -> "Driving"))

      val crash = ConditionalProbabilityTable3[Boolean, Double, String, String, String](
        Map(true -> 0.9, false -> 0.1))

      1 must be equalTo 1
    }
  }

}