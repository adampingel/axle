---
layout: page
title: Smith-Waterman
permalink: /tutorial/smith_waterman/
---

See the Wikipedia page on the
[Smith-Waterman](https://en.wikipedia.org/wiki/Smith%E2%80%93Waterman_algorithm) algorithm.

## Example

Imports and implicits

```tut:book:silent
import org.jblas.DoubleMatrix

import cats.implicits._

import spire.implicits.IntAlgebra

import axle.bio._
import SmithWatermanDefaults._
import SmithWaterman.optimalAlignment

implicit val laJblasInt = axle.jblas.linearAlgebraDoubleMatrix[Int]
```

Setup

```tut:book
val dna3 = "ACACACTA"
val dna4 = "AGCACACA"
```

Align the sequences

```tut:book
val swAlignment = optimalAlignment[IndexedSeq[Char], Char, DoubleMatrix, Int, Int](
  dna3, dna4, w, mismatchPenalty, gap)
```

Compute distance of the sequences

```tut:book
import spire.implicits.DoubleAlgebra

val space = SmithWatermanMetricSpace[IndexedSeq[Char], Char, DoubleMatrix, Int, Int](w, mismatchPenalty)

space.distance(dna3, dna4)
```
