package axle.visualize

import org.specs2.mutable.Specification

class PokerHandChartSpec extends Specification {

  "poker hand chart" should {
    "work" in {

      import axle.game.cards.Deck
      import axle.game.poker.PokerHand
      import axle.game.poker.PokerHandCategory
      import spire.compat.ordering

      def winnerFromHandSize(handSize: Int) =
        Deck().cards.take(handSize).combinations(5).map(PokerHand(_)).toList.max

      val data: IndexedSeq[(PokerHandCategory, Int)] =
        for {
          handSize <- 5 to 9
          trial <- 1 to 1000
        } yield (winnerFromHandSize(handSize).category, handSize)

      // TODO the inner Int should be (n: Int) => s"5 from $n"

      import spire.implicits.IntAlgebra
      import axle.visualize.BarChartGrouped

      val chart = BarChartGrouped[PokerHandCategory, Int, Int, Map[(PokerHandCategory, Int), Int]](
        data.tally.withDefaultValue(0),
        yAxisLabel = Some("instances of hand category by initial hand size (1000 trial for each hand size)"),
        title = Some("Poker Hands"))

      import axle.web._

      SVG[BarChartGrouped[PokerHandCategory, Int, Int, Map[(PokerHandCategory, Int), Int]]]

      1 must be equalTo 1
    }
  }

}