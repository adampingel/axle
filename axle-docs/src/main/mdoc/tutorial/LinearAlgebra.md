---
layout: page
title: Linear Algebra
permalink: /tutorial/linear_algebra/
---

A `LinearAlgebra` typeclass.

The `axle-jblas` spoke provides witnesses for JBLAS matrices.

The default jblas matrix `toString` isn't very readable,
so this tutorial wraps most results in the Axle `string` function,
invoking the `cats.Show` witness for those matrices.

## Imports and implicits

Import JBLAS and Axle's `LinearAlgebra` witness for it.

```scala mdoc:silent
import axle._
import axle.jblas._
import axle.syntax.linearalgebra.matrixOps
import spire.implicits.DoubleAlgebra

implicit val laJblasDouble = axle.jblas.linearAlgebraDoubleMatrix[Double]
import laJblasDouble._
```

## Creating Matrices

```scala mdoc
string(ones(2, 3))

string(ones(1, 4))

string(ones(4, 1))
```

## Creating matrices from arrays

```scala mdoc
string(fromColumnMajorArray(2, 2, List(1.1, 2.2, 3.3, 4.4).toArray))

string(fromColumnMajorArray(2, 2, List(1.1, 2.2, 3.3, 4.4).toArray).t)

val m = fromColumnMajorArray(4, 5, (1 to 20).map(_.toDouble).toArray)
string(m)
```

## Random matrices

```scala mdoc
val r = rand(3, 3)

string(r)
```

## Matrices defined by functions

```scala mdoc
string(matrix(4, 5, (r, c) => r / (c + 1d)))

string(matrix(4, 5, 1d,
  (r: Int) => r + 0.5,
  (c: Int) => c + 0.6,
  (r: Int, c: Int, diag: Double, left: Double, right: Double) => diag))
```

## Metadata

```scala mdoc
val x = fromColumnMajorArray(3, 1, Vector(4.0, 5.1, 6.2).toArray)
string(x)

val y = fromColumnMajorArray(3, 1, Vector(7.3, 8.4, 9.5).toArray)
string(y)

x.isEmpty

x.isRowVector

x.isColumnVector

x.isSquare

x.isScalar

x.rows

x.columns

x.length
```

## Accessing columns, rows, and elements

```scala mdoc
string(x.column(0))

string(x.row(1))

x.get(2, 0)

val fiveByFive = fromColumnMajorArray(5, 5, (1 to 25).map(_.toDouble).toArray)

string(fiveByFive)

string(fiveByFive.slice(1 to 3, 2 to 4))

string(fiveByFive.slice(0.until(5,2), 0.until(5,2)))
```

## Negate, Transpose, Power

```scala mdoc
string(x.negate)

string(x.transpose)

// x.log
// x.log10

string(x.pow(2d))
```

## Mins, Maxs, Ranges, and Sorts

```scala mdoc
r.max

r.min

// r.ceil
// r.floor

string(r.rowMaxs)

string(r.rowMins)

string(r.columnMaxs)

string(r.columnMins)

string(rowRange(r))

string(columnRange(r))

string(r.sortRows)

string(r.sortColumns)

string(r.sortRows.sortColumns)
```

## Statistics

```scala mdoc
string(r.rowMeans)

string(r.columnMeans)

// median(r)

string(sumsq(r))

string(std(r))

string(cov(r))

string(centerRows(r))

string(centerColumns(r))

string(zscore(r))
```

## Principal Component Analysis

```scala mdoc
val (u, s) = pca(r, 0.95)

string(u)

string(s)
```

## Horizontal and vertical concatenation

```scala mdoc
string(x aside y)

string(x atop y)
```

## Addition and subtraction

```scala mdoc
val x = ones(2, 3)

string(x)
```

Matrix addition

```scala mdoc
import spire.implicits.additiveSemigroupOps

string(x + x)
```

Scalar addition (JBLAS method)

```scala mdoc
string(x.addScalar(1.1))
```

Matrix subtraction

```scala mdoc
import spire.implicits.additiveGroupOps

string(x - x)
```

Scalar subtraction (JBLAS method)

```scala mdoc
string(x.subtractScalar(0.2))
```

## Multiplication and Division

Scalar multiplication

```scala mdoc
string(x.multiplyScalar(3d))
```

Matrix multiplication

```scala mdoc
import spire.implicits.multiplicativeSemigroupOps

string(x * x.transpose)
```

Scalar division (JBLAS method)

```scala mdoc
string(x.divideScalar(100d))
```

## Map element values

```scala mdoc
implicit val endo = axle.jblas.endoFunctorDoubleMatrix[Double]
import axle.syntax.endofunctor.endofunctorOps

val half = ones(3, 3).map(_ / 2d)

string(half)
```

## Boolean operators

```scala mdoc
string(r lt half)

string(r le half)

string(r gt half)

string(r ge half)

string(r eq half)

string(r ne half)

string((r lt half) or (r gt half))

string((r lt half) and (r gt half))

string((r lt half) xor (r gt half))

string((r lt half) not)
```

## Higher order methods

```scala mdoc
string(m.map(_ + 1))

string(m.map(_ * 10))

// m.foldLeft(zeros(4, 1))(_ + _)

string(m.foldLeft(ones(4, 1))(_ mulPointwise _))

// m.foldTop(zeros(1, 5))(_ + _)

string(m.foldTop(ones(1, 5))(_ mulPointwise _))
```
