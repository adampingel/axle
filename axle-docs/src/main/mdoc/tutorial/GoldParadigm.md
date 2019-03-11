---
layout: page
title: Gold Paradigm
permalink: /tutorial/gold_paradigm/
---

Models the Gold Paradigm.

## Example

Imports

```scala mdoc:silent
import cats.implicits._
import axle._
import axle.lx._
import Gold._
```

Setup

```scala mdoc:silent
val mHi = Morpheme("hi")
val mIm = Morpheme("I'm")
val mYour = Morpheme("your")
val mMother = Morpheme("Mother")
val mShut = Morpheme("shut")
val mUp = Morpheme("up")

val Σ = Vocabulary(Set(mHi, mIm, mYour, mMother, mShut, mUp))

val s1 = mHi :: mIm :: mYour :: mMother :: Nil
val s2 = mShut :: mUp :: Nil

val ℒ = Language(Set(s1, s2))

val T = Text(s1 :: ♯ :: ♯ :: s2 :: ♯ :: s2 :: s2 :: Nil)

val ɸ = MemorizingLearner()
```

Usage

```scala mdoc
ɸ.guesses(T).
 find(_.ℒ === ℒ).
 map(finalGuess => "well done, ɸ").
 getOrElse("ɸ never made a correct guess")

ℒ

T

T.isFor(ℒ)
```
