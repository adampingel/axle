---
layout: page
title: Clusters Federalist Papers with k-Means
permalink: /tutorial/cluster_federalist_papers_k_means/
---

Imports

```scala mdoc:silent
import axle.data.FederalistPapers._
```

The Federalist articles:

```scala mdoc
articles.size
```

Construct a `Corpus` object to assist with content analysis

```scala mdoc
import axle.nlp._
import axle.nlp.language.English

val corpus = Corpus(articles.map(_.text), English)
```

Define a feature extractor using top words and bigrams.

```scala mdoc
val frequentWords = corpus.wordsMoreFrequentThan(100)

val topBigrams = corpus.topKBigrams(200)

val numDimensions = frequentWords.size + topBigrams.size

import spire.algebra.Ring
implicit val ringLong: Ring[Long] = spire.implicits.LongAlgebra

def featureExtractor(fp: Article): List[Double] = {
  import axle.enrichGenSeq

  val tokens = English.tokenize(fp.text.toLowerCase)
  val wordCounts = tokens.tally[Long]
  val bigramCounts =  bigrams(tokens).tally[Long]
  val wordFeatures = frequentWords.map(wordCounts(_) + 0.1)
  val bigramFeatures = topBigrams.map(bigramCounts(_) + 0.1)
  wordFeatures ++ bigramFeatures
}
```

Place a `MetricSpace` implicitly in scope that defines the space in which to
measure similarity of Articles.

```scala mdoc:silent
import spire.algebra._

import axle.algebra.distance._
import axle.algebra.distance.Euclidean

import org.jblas.DoubleMatrix
import axle.jblas.linearAlgebraDoubleMatrix

implicit val fieldDouble: Field[Double] = spire.implicits.DoubleAlgebra
implicit val nrootDouble: NRoot[Double] = spire.implicits.DoubleAlgebra

implicit val space = {
  implicit val ringInt: Ring[Int] = spire.implicits.IntAlgebra
  implicit val inner = axle.jblas.rowVectorInnerProductSpace[Int, Int, Double](numDimensions)
  new Euclidean[DoubleMatrix, Double]
}
```

Create 4 clusters using k-Means

```scala mdoc:silent
import axle.ml.KMeans
import axle.ml.PCAFeatureNormalizer
```

```scala mdoc
import cats.implicits._
import spire.random.Generator.rng

val normalizer = (PCAFeatureNormalizer[DoubleMatrix] _).curried.apply(0.98)

val classifier = KMeans[Article, List, DoubleMatrix](
  articles,
  N = numDimensions,
  featureExtractor,
  normalizer,
  K = 4,
  iterations = 100)(rng)
```

Show cluster vs author in a confusion matrix:

```scala mdoc:silent
import axle.ml.ConfusionMatrix
```

```scala mdoc
val confusion = ConfusionMatrix[Article, Int, String, Vector, DoubleMatrix](
  classifier,
  articles.toVector,
  _.author,
  0 to 3)

confusion.show
```