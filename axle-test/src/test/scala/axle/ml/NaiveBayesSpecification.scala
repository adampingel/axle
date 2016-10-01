package axle.ml

import org.specs2.mutable.Specification

import axle.string
import axle.stats.UnknownDistribution0
import axle.stats.rationalProbabilityDist
import spire.implicits.BooleanStructure
import spire.implicits.StringOrder
import spire.math.Rational

object NaiveBayesSpecification extends Specification {

  case class Tennis(outlook: String, temperature: String, humidity: String, wind: String, play: Boolean)

  val data =
    Tennis("Sunny", "Hot", "High", "Weak", false) ::
      Tennis("Sunny", "Hot", "High", "Strong", false) ::
      Tennis("Overcast", "Hot", "High", "Weak", true) ::
      Tennis("Rain", "Mild", "High", "Weak", true) ::
      Tennis("Rain", "Cool", "Normal", "Weak", true) ::
      Tennis("Rain", "Cool", "Normal", "Strong", false) ::
      Tennis("Overcast", "Cool", "Normal", "Strong", true) ::
      Tennis("Sunny", "Mild", "High", "Weak", false) ::
      Tennis("Sunny", "Cool", "Normal", "Weak", true) ::
      Tennis("Rain", "Mild", "Normal", "Weak", true) ::
      Tennis("Sunny", "Mild", "Normal", "Strong", true) ::
      Tennis("Overcast", "Mild", "High", "Strong", true) ::
      Tennis("Overcast", "Hot", "Normal", "Weak", true) ::
      Tennis("Rain", "Mild", "High", "Strong", false) :: Nil

  "naive bayes tennis classifier" should {
    "predict play in dataset #1" in {

      val classifier1 = NaiveBayesClassifier[Tennis, String, Boolean, List[Tennis], List[Boolean], Rational](
        data,
        List(
          UnknownDistribution0[String, Rational](Vector("Sunny", "Overcast", "Rain"), "Outlook"),
          UnknownDistribution0[String, Rational](Vector("Hot", "Mild", "Cool"), "Temperature"),
          UnknownDistribution0[String, Rational](Vector("High", "Normal", "Low"), "Humidity"),
          UnknownDistribution0[String, Rational](Vector("Weak", "Strong"), "Wind")),
        UnknownDistribution0[Boolean, Rational](Vector(true, false), "Play"),
        (t: Tennis) => t.outlook :: t.temperature :: t.humidity :: t.wind :: Nil,
        (t: Tennis) => t.play)

      val performance1 = ClassifierPerformance[Rational, Tennis, List[Tennis], List[(Rational, Rational, Rational, Rational)]](
        data,
        classifier1,
        _.play)

      performance1.tp must be equalTo 9
      performance1.fp must be equalTo 1
      performance1.tn must be equalTo 4
      performance1.fn must be equalTo 0
      performance1.precision must be equalTo Rational(9, 10)
      performance1.recall must be equalTo Rational(1)
      performance1.accuracy must be equalTo Rational(13, 14)
      performance1.specificity must be equalTo Rational(4, 5)
      performance1.f1Score must be equalTo Rational(18, 19)
    }
  }

  // http://www.dhgarrette.com/nlpclass/assignments/a2classification.html
  val data2 =
    Tennis("Sunny", "Cool", "High", "Strong", false) ::
      Tennis("Overcast", "Cool", "Normal", "Weak", true) ::
      Tennis("Sunny", "Hot", "Normal", "Weak", true) ::
      Tennis("Rain", "Hot", "High", "Strong", false) ::
      Tennis("Sunny", "Cool", "Normal", "Weak", true) ::
      Tennis("Overcast", "Hot", "High", "Strong", false) ::
      Tennis("Sunny", "Mild", "High", "Weak", true) ::
      Tennis("Overcast", "Mild", "Normal", "Strong", true) ::
      Tennis("Rain", "Cool", "Normal", "Strong", false) ::
      Tennis("Overcast", "Cool", "Normal", "Strong", true) ::
      Tennis("Rain", "Hot", "Normal", "Weak", true) ::
      Tennis("Sunny", "Cool", "High", "Weak", true) ::
      Tennis("Rain", "Hot", "Normal", "Strong", false) :: Nil

  /**
   * Total: 13
   *
   * Outlook    : Sunny  5, Overcast 4, Rain 4
   * Temperature: Hot    5, Mild     2, Cool 6
   * Humidity   : High   5, Normal   8
   * Wind       : Strong 7, Weak     6
   * Play       : true   8, false    5
   *
   * All predictions of 'Play' by Naive Bayes are correct
   */

  "naive bayes tennis classifier" should {
    "predict play in dataset #2" in {

      val classifier2 = NaiveBayesClassifier[Tennis, String, Boolean, List[Tennis], List[Boolean], Rational](
        data2,
        List(
          UnknownDistribution0[String, Rational](Vector("Sunny", "Overcast", "Rain"), "Outlook"),
          UnknownDistribution0[String, Rational](Vector("Hot", "Mild", "Cool"), "Temperature"),
          UnknownDistribution0[String, Rational](Vector("High", "Normal", "Low"), "Humidity"),
          UnknownDistribution0[String, Rational](Vector("Weak", "Strong"), "Wind")),
        UnknownDistribution0[Boolean, Rational](Vector(true, false), "Play"),
        (t: Tennis) => t.outlook :: t.temperature :: t.humidity :: t.wind :: Nil,
        (t: Tennis) => t.play)

      val performance2 = ClassifierPerformance[Rational, Tennis, List[Tennis], List[(Rational, Rational, Rational, Rational)]](
        data, // Note: not the same as the training dataset
        classifier2,
        _.play)

      performance2.tp must be equalTo 9
      performance2.fp must be equalTo 3
      performance2.tn must be equalTo 2
      performance2.fn must be equalTo 0
      performance2.precision must be equalTo Rational(3, 4)
      performance2.recall must be equalTo Rational(1)
      performance2.specificity must be equalTo Rational(2, 5)
      performance2.accuracy must be equalTo Rational(11, 14)
      performance2.f1Score must be equalTo Rational(6, 7)
      string(performance2) must contain("F1")
    }
  }

}