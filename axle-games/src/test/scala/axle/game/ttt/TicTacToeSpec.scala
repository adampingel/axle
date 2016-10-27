package axle.game.ttt

import axle.dropOutput
import axle.game._
import axle.game.Strategies._
import org.specs2.mutable._

class TicTacToeSpec extends Specification {

  import axle.game.ttt.evGame._

  val x = Player("X", "Player X")
  val o = Player("O", "Player O")

  val game = TicTacToe(3,
    x, interactiveMove, dropOutput,
    o, interactiveMove, dropOutput)

  "game" should {
    "define intro message, have 9 positions" in {

      introMessage(game) must contain("Moves are")
      game.numPositions must be equalTo 9
    }
  }

  "random game" should {

    val rGame = TicTacToe(3,
      x, randomMove, dropOutput,
      o, randomMove, dropOutput)

    "produce moveStateStream" in {
      moveStateStream(rGame, startState(rGame)).take(3).length must be equalTo 3
    }

    "play" in {
      val endState = play(rGame, startState(rGame), false)
      moves(endState, rGame).length must be equalTo 0
    }

    "product game stream" in {
      val games = gameStream(rGame, startState(rGame), false).take(2)
      games.length must be equalTo 2
    }

  }

  "start state" should {
    "display movement key to player x, and have 9 moves available to x" in {
      displayStateTo(startState(game), x, game) must contain("Movement Key")
    }
  }

  "startFrom" should {
    "simply return the start state" in {
      val state = startState(game)
      val move = moves(state, game).head
      val nextState = applyMove(state, game, move)
      val newStart = startFrom(game, nextState).get
      moves(newStart, game).length must be equalTo 9
    }
  }

  "starting moves" should {
    "be nine-fold, display to O with 'put an', and have string descriptions that contain 'upper'" in {

      val startingMoves = moves(startState(game), game)

      displayMoveTo(game, x, startingMoves.head, o) must contain("put an")
      startingMoves.length must be equalTo 9
      startingMoves.map(_.description).mkString(",") must contain("upper")
    }
    "be defined for 4x4 game" in {
      val bigGame = TicTacToe(4,
        x, randomMove, dropOutput,
        o, randomMove, dropOutput)
      val startingMoves = moves(startState(bigGame), bigGame)
      startingMoves.map(_.description).mkString(",") must contain("16")
    }
  }

  "interactive player" should {
    "print various messages" in {

      val firstMove = TicTacToeMove(2, game.boardSize)
      val secondState = applyMove(startState(game), game, firstMove)

      val evGame = implicitly[Game[TicTacToe, TicTacToeState, TicTacToeOutcome, TicTacToeMove]]

      val m = secondState.moverOpt.get
      evGame.parseMove(game, "14") must be equalTo Left("Please enter a number between 1 and 9")
      evGame.parseMove(game, "foo") must be equalTo Left("foo is not a valid move.  Please select again")

      evGame.parseMove(game, "1").right.flatMap(move => evGame.isValid(game, secondState, move)).right.toOption.get.position must be equalTo 1
      evGame.parseMove(game, "2").right.flatMap(move => evGame.isValid(game, secondState, move)) must be equalTo Left("That space is occupied.")
    }
  }

  "random strategy" should {
    "make a move" in {

      val mover = randomMove(evGame)
      val m = mover(startState(game), game)

      m.position must be greaterThan 0
    }
  }

  "A.I. strategy" should {
    "make a move" in {

      val firstMove = TicTacToeMove(2, game.boardSize)

      val h = (outcome: TicTacToeOutcome, p: Player) =>
        outcome.winner.map(wp => if (wp == p) 1d else -1d).getOrElse(0d)

      import spire.implicits.DoubleAlgebra
      val ai4 = aiMover[TicTacToe, TicTacToeState, TicTacToeOutcome, TicTacToeMove, Double](
        4, outcomeRingHeuristic(game, h))

      val secondState = applyMove(startState(game), game, firstMove)

      val move = ai4(secondState, game)

      move.position must be greaterThan 0
    }
  }

  "7-move x diagonal" should {
    "be a victory for x" in {

      def xMove(state: TicTacToeState, game: TicTacToe): String = moves(state, game).size match {
        case 9 => "1"
        case 7 => "3"
        case 5 => "5"
        case 3 => "7"
      }

      def oMove(state: TicTacToeState, game: TicTacToe): String = moves(state, game).size match {
        case 8 => "2"
        case 6 => "4"
        case 4 => "6"
      }

      val game = TicTacToe(3,
        x, hardCodedStrategy(xMove), dropOutput,
        o, hardCodedStrategy(oMove), dropOutput)

      val start = startState(game)
      val lastState = moveStateStream(game, start).last._3
      val out = outcome(lastState, game).get
      displayOutcomeTo(game, out, x) must contain("You beat")
      displayOutcomeTo(game, out, o) must contain("beat You")
      out.winner.get should be equalTo x
    }
  }

  "7-move o diagonal" should {
    "be a victory for o" in {

      def xMove(state: TicTacToeState, game: TicTacToe): String = moves(state, game).size match {
        case 9 => "2"
        case 7 => "4"
        case 5 => "6"
        case 3 => "8"
      }

      def oMove(state: TicTacToeState, game: TicTacToe): String = moves(state, game).size match {
        case 8 => "3"
        case 6 => "5"
        case 4 => "7"
      }

      val game = TicTacToe(3,
        x, hardCodedStrategy(xMove), dropOutput,
        o, hardCodedStrategy(oMove), dropOutput)

      val start = startState(game)
      val lastState = moveStateStream(game, start).last._3
      val winnerOpt = outcome(lastState, game).flatMap(_.winner)
      winnerOpt should be equalTo (Some(o))
    }
  }

  "9 move tie" should {
    "result in no-winner outcome" in {

      def xMove(state: TicTacToeState, game: TicTacToe): String = moves(state, game).size match {
        case 9 => "1"
        case 7 => "3"
        case 5 => "5"
        case 3 => "8"
        case 1 => "6"
      }

      def oMove(state: TicTacToeState, game: TicTacToe): String = moves(state, game).size match {
        case 8 => "2"
        case 6 => "4"
        case 4 => "7"
        case 2 => "9"
      }

      val game = TicTacToe(3,
        x, hardCodedStrategy(xMove), dropOutput,
        o, hardCodedStrategy(oMove), dropOutput)

      val start = startState(game)
      val lastState = moveStateStream(game, start).last._3

      val winnerOpt = outcome(lastState, game).flatMap(_.winner)
      winnerOpt should be equalTo (None)
    }
  }

}
