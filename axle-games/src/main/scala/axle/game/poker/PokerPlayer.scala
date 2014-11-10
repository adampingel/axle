package axle.game.poker

import axle.game._
import spire.algebra.Eq

object PokerPlayer {

  implicit def ppEq: Eq[PokerPlayer] = new Eq[PokerPlayer] {
    def eqv(x: PokerPlayer, y: PokerPlayer): Boolean = x.equals(y)
  }
  
}

abstract class PokerPlayer(id: String, description: String)(implicit game: Poker)
extends Player[Poker](id, description)
