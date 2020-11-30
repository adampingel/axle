package axle

import cats.Monad
import cats.kernel.Order
import cats.implicits._

import spire.algebra.Field
import spire.algebra.Ring
import spire.random.Generator
import spire.random.Dist
import spire.implicits.additiveGroupOps

import axle.algebra._
import axle.probability._
import axle.syntax.sampler._

package object game {

  def nextMoveState[
    G, S, O, M, MS, MM, V,
    PM[_, _],
    F[_]: Monad](
    game:      G,
    fromState: S,
    strategies: Player => MS => F[PM[M, V]],
    gen:       Generator)(
    implicit
    evGame: Game[G, S, O, M, MS, MM],
    prob:   Sampler[PM],
    distV:  Dist[V],
    ringV:  Ring[V],
    orderV: Order[V]): F[Option[(S, M, S)]] =
    evGame.mover(game, fromState) map { mover => {
      val strategyFn: MS => F[PM[M, V]] = strategies(mover)
      val fStrategy: F[PM[M, V]] = strategyFn(evGame.maskState(game, fromState, mover))
      fStrategy map { strategy =>
        val move: M = strategy.sample(gen)
        val toState: S = evGame.applyMove(game, fromState, move)
        Option((fromState, move, toState))
      }
    }} getOrElse(Monad[F].pure(Option.empty[(S, M, S)]))

  def moveStateStream[
    G, S, O, M, MS, MM, V,
    PM[_, _],
    F[_]: Monad](
    game:      G,
    fromState: S,
    strategies: Player => MS => F[PM[M, V]],
    gen:       Generator)(
    implicit
    evGame: Game[G, S, O, M, MS, MM],
    prob:   Sampler[PM],
    distV:  Dist[V],
    ringV:  Ring[V],
    orderV: Order[V]): F[LazyList[(S, M, S)]] =
    chain[S, (S, M, S), F, LazyList](
      fromState,
      (s: S) => nextMoveState(game, s, strategies, gen),
      _._3,
      LazyList.empty,
      b => ll => ll.prepended(b)
    )

  def moveFromRandomState[
    G, S, O, M, MS, MM, V,
    PM[_, _],
    F[_]: Monad](
    game:      G,
    stateModel: PM[S, V],
    strategies: Player => MS => F[PM[M, V]],
    mapToProb: Map[S, V] => PM[S, V], // TODO replace this
    gen:       Generator)(
    implicit
    evGame: Game[G, S, O, M, MS, MM],
    prob:   Sampler[PM],
    kolm:   Kolmogorov[PM],
    bayes:  Bayes[PM],
    monad:  Monad[PM[?, V]],
    eqS:    cats.kernel.Eq[S],
    eqM:    cats.kernel.Eq[M],
    distV:  Dist[V],
    fieldV: Field[V],
    orderV: Order[V]): F[(Option[(S, M)], PM[S, V])] = {

    val openStateModel: PM[S, V] = bayes.filter(stateModel)(RegionIf(evGame.mover(game, _).isDefined))

    val fromState: S = prob.sample(openStateModel)(gen)
    // val probabilityOfFromState: V = prob.probabilityOf(stateModel)(RegionEq(fromState))

    evGame.mover(game, fromState).map { mover => {
      val strategyFn = strategies(mover)
      val fStrategy: F[PM[M, V]] = strategyFn(evGame.maskState(game, fromState, mover))
      fStrategy map { strategy =>

        val move = strategy.sample(gen)
        val toState = evGame.applyMove(game, fromState, move)

        import cats.syntax.all._
        if( fromState === toState ) {
          (Some((fromState, move)), stateModel)
        } else {
          val probabilityOfMove: V = kolm.probabilityOf(strategy)(RegionEq(move))
          // val mass = probabilityOfFromState * probabilityOfMove // TODO scale mass down
          val redistributed = monad.flatMap(stateModel)( s =>
            if( s === fromState) {
              mapToProb(Map(fromState -> (Field[V].one - probabilityOfMove), toState -> probabilityOfMove))
            } else {
              monad.pure(s)
            })
          (Option((fromState, move)), redistributed)
        }
      }
    }} getOrElse {
      Monad[F].pure[(Option[(S, M)], PM[S, V])]((None, stateModel))
    }
  }
  
  def mapNextState[
    G, S, O, M, MS, MM, V,
    PM[_, _], T,
    F[_]: Monad](
    game:        G,
    fromState:   S,
    strategies: Player => MS => F[PM[M, V]],
    strategyToT: (G, S, PM[M, V]) => T,
    gen:         Generator)(
    implicit
    evGame: Game[G, S, O, M, MS, MM],
    prob:   Sampler[PM],
    distV: Dist[V],
    ringV:  Ring[V],
    orderV: Order[V]): F[Option[(S, T, S)]] =
    evGame.mover(game, fromState).map { mover => {
      val strategyFn = strategies(mover)
      val fStrategy: F[PM[M, V]] = strategyFn(evGame.maskState(game, fromState, mover))
      fStrategy map { strategy =>
        val move = strategy.sample(gen)
        val toState = evGame.applyMove(game, fromState, move)
        Option((fromState, strategyToT(game, fromState, strategy), toState))
      }
  }} getOrElse(Monad[F].pure(Option.empty[(S, T, S)]))

  def stateStreamMap[
    G, S, O, M, MS, MM, V,
    PM[_, _], T,
    F[_]: Monad](
    game:        G,
    fromState:   S,
    strategies: Player => MS => F[PM[M, V]],
    strategyToT: (G, S, PM[M, V]) => T,
    gen:         Generator)(
    implicit
    evGame: Game[G, S, O, M, MS, MM],
    prob:   Sampler[PM],
    distV: Dist[V],
    ringV:  Ring[V],
    orderV: Order[V]): F[LazyList[(S, T, S)]] =
    chain[S, (S, T, S), F, LazyList](
      fromState,
      (s: S) => mapNextState(game, s, strategies, strategyToT, gen),
      _._3,
      LazyList.empty,
      b => ll => ll.prepended(b)
    )

  def nextStateStrategyMoveState[
    G, S, O, M, MS, MM, V,
    PM[_, _],
    F[_]: Monad](
    game:        G,
    fromState:   S,
    strategies: Player => MS => F[PM[M, V]],
    gen:         Generator)(
    implicit
    evGame: Game[G, S, O, M, MS, MM],
    prob:   Sampler[PM],
    distV:  Dist[V],
    ringV:  Ring[V],
    orderV: Order[V]): F[Option[(S, (PM[M, V], M), S)]] =
    evGame.mover(game, fromState).map { mover => {
      val strategyFn = strategies(mover)
      val fStrategy: F[PM[M, V]] = strategyFn(evGame.maskState(game, fromState, mover))
      fStrategy map { strategy =>
        val move = strategy.sample(gen)
        val toState = evGame.applyMove(game, fromState, move)
        Option((fromState, (strategy, move), toState))
      }
  }} getOrElse(Monad[F].pure(Option.empty))

  def stateStrategyMoveStream[
    G, S, O, M, MS, MM, V,
    PM[_, _],
    F[_]: Monad](
    game:        G,
    fromState:   S,
    strategies: Player => MS => F[PM[M, V]],
    gen:         Generator)(
    implicit
    evGame: Game[G, S, O, M, MS, MM],
    prob:   Sampler[PM],
    distV:  Dist[V],
    ringV:  Ring[V],
    orderV: Order[V]): F[LazyList[(S, (PM[M, V], M), S)]] =
    chain[S, (S, (PM[M, V], M), S), F, LazyList](
      fromState,
      (s: S) => nextStateStrategyMoveState(game, s, strategies, gen),
      _._3,
      LazyList.empty,
      b => ll => ll.prepended(b)
    )

  def play[G, S, O, M, MS, MM, V, PM[_, _], F[_]: Monad](
    game: G,
    strategies: Player => MS => F[PM[M, V]],
    gen: Generator)(
    implicit
    evGame:   Game[G, S, O, M, MS, MM],
    prob:     Sampler[PM],
    distV:    Dist[V],
    ringV:    Ring[V],
    orderV:   Order[V]): F[S] =
    play(game, strategies, evGame.startState(game), gen)

  def play[G, S, O, M, MS, MM, V, PM[_, _], F[_]: Monad](
    game:  G,
    strategies: Player => MS => F[PM[M, V]],
    start: S,
    gen:   Generator)(
    implicit
    evGame:   Game[G, S, O, M, MS, MM],
    prob:     Sampler[PM],
    distV: Dist[V],
    ringV: Ring[V],
    orderV: Order[V]): F[S] =
    moveStateStream(game, start, strategies, gen) map { _.last._3 }

  def gameStream[G, S, O, M, MS, MM, V, PM[_, _], F[_]: Monad](
    game:  G,
    strategies: Player => MS => F[PM[M, V]],
    start: S,
    gen:   Generator)(
    implicit
    evGame:   Game[G, S, O, M, MS, MM],
    prob:     Sampler[PM],
    distV:    Dist[V],
    ringV:    Ring[V],
    orderV:   Order[V]): F[LazyList[S]] =
    chain[S, (S, Unit, S), F, LazyList](
      start,
      (s: S) => {
        play(game, strategies, s, gen) map { end =>
          Option((end, (), evGame.startFrom(game, end).get))
        }},
      _._3,
     LazyList.empty,
     b => ll => ll.prepended(b)
    ).map { _.map { _._1 } } // Or should this be _3?

  def playContinuously[G, S, O, M, MS, MM, V, PM[_, _], F[_]: Monad](
    game:  G,
    strategies: Player => MS => F[PM[M, V]],
    start: S,
    gen:   Generator)(
    implicit
    evGame:   Game[G, S, O, M, MS, MM],
    prob:     Sampler[PM],
    distV:    Dist[V],
    ringV:    Ring[V],
    orderV:   Order[V]): F[S] =
    gameStream(game, strategies, start, gen).map(_.last)

}
