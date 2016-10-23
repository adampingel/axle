package axle.game

trait State[G, S, O, M] {

  def mover(s: S): Option[Player]

  def applyMove(
    state: S,
    game: G,
    move: M)(
      implicit evGame: Game[G, S, O, M]): S

  def displayTo(state: S, viewer: Player, game: G)(
    implicit evGame: Game[G, S, O, M]): String

  def outcome(s: S, game: G): Option[O]

  def moves(s: S, game: G): Seq[M]

}
