package axle

import scala.language.implicitConversions
import scala.collection.immutable.TreeMap

import cats.Functor
import cats.Order.catsKernelOrderingForOrder

import spire.algebra._
import spire.math.Rational
import spire.math.Real
import spire.math.Real.apply
import spire.implicits.additiveGroupOps
import spire.implicits.additiveSemigroupOps

package object algebra {

  // basic functions

  /**
   * dummy is not to be used widely, but is used for for scanLeft, where
   * it's often desirable to provide a throw-away value as the first argument
   * without using an Option type for an already complicated method signature.
   * A better work-around would be an alternate version of scanLeft that had
   * this behavior built in.
   *
   * Something like this:
   *
   * def scanLeftDropFirst[A, Repr, B, C, That](
   *   tl: scala.collection.TraversableLike[A, Repr])(z: C)(op: ((B, C), A) ⇒ (B, C))(
   *   implicit bf: scala.collection.generic.CanBuildFrom[Repr, (B, C), That]) =
   *   tl.scanLeft((axle.dummy[B], z))(op) // Caller should .drop(1). TODO do that here
   */

  def dummy[T]: T = null.asInstanceOf[T]

  def ignore[T]: T => Unit = (t: T) => {}

  def id[A](x: A): A = x

  implicit class EnrichedRinged[N](x: N)(implicit ringN: Ring[N]) {

    // a.k.a. `…`

    def etc: Iterable[N] =
      new Iterable[N] {
        def iterator: Iterator[N] = new Iterator[N] {

          var current = x

          def next(): N = {
            val rc = current
            current = ringN.plus(current, ringN.one)
            rc
          }

          def hasNext: Boolean = true
        }
      }

  }

  def tensorProduct[T](xs: Vector[T], ys: Vector[T])(implicit multT: MultiplicativeSemigroup[T]): Vector[T] = 
    for {
      x <- xs
      y <- ys
    } yield multT.times(x, y)

  implicit def catsifyAdditiveGroup[T](ag: _root_.algebra.ring.AdditiveGroup[T]): cats.kernel.Group[T] =
    new cats.kernel.Group[T] {
      def inverse(a: T): T = ag.negate(a)
      def empty: T = ag.zero
      def combine(x: T, y: T): T = ag.plus(x, y)
    }

  // missing Eq witnesses

  implicit def eqIterable[T](implicit eqT: Eq[T]): Eq[Iterable[T]] =
    (x, y) =>
      x.size === y.size && x.zip(y).forall({ case (p, q) => eqT.eqv(p, q) })

  implicit def eqTreeMap[K, V](implicit eqK: Eq[K], eqV: Eq[V]): Eq[TreeMap[K, V]] = {
    val eqOptV = Eq[Option[V]]
    (x, y) =>
      Eq[Iterable[K]].eqv(x.keys, y.keys) && x.keySet.forall(k => eqOptV.eqv(x.get(k), y.get(k)))
  }

  implicit def eqIndexedSeq[T](implicit eqT: Eq[T]): Eq[IndexedSeq[T]] =
    (l: IndexedSeq[T], r: IndexedSeq[T]) =>
      l.size === r.size && (0 until l.size).forall( i => eqT.eqv(l(i), r(i)))

  implicit val functorIndexedSeq: Functor[IndexedSeq] =
    new Functor[IndexedSeq] {
      def map[A, B](as: IndexedSeq[A])(f: A => B): IndexedSeq[B] =
        as.map(f)
    }

  implicit val functorSeq: Functor[Seq] =
    new Functor[Seq] {
      def map[A, B](as: Seq[A])(f: A => B): Seq[B] =
        as.map(f)
    }

  // Function application patterns

  def applyK[N](f: N => N, x0: N, k: Int): N =
    (1 to k).foldLeft(x0)({ case (x, _) => f(x) })

  // Iterator methods

  def applyForever[N](f: N => N, x0: N): Iterator[N] =
    Iterator
      .continually(())
      .scanLeft(x0)({ case (x, _) => f(x) })

  def trace[N](f: N => N, x0: N): Iterator[(N, Set[N])] =
    Iterator
      .continually(())
      .scanLeft((x0, Set.empty[N]))({
        case ((x, points), _) =>
          (f(x), points + x)
      })

  /**
   * mergeStreams takes streams that are ordered w.r.t. Order[T]
   *
   */

  def mergeStreams[T](streams: Seq[LazyList[T]])(
    implicit
    orderT: Order[T]): LazyList[T] = {

    val frontier = streams.flatMap(_.headOption)

    if (frontier.size === 0) {
      LazyList.empty
    } else {
      val head = frontier.min
      LazyList.cons(head, mergeStreams(streams.map(_.dropWhile(_ === head))))
    }
  }

  def filterOut[T](stream: LazyList[T], toRemove: LazyList[T])(implicit orderT: Order[T]): LazyList[T] =
    if (stream.isEmpty || toRemove.isEmpty) {
      stream
    } else {
      val remove = toRemove.head
      stream.takeWhile(_ < remove) ++ filterOut(stream.dropWhile(_ <= remove), toRemove.drop(1))
    }

  def lazyListsFrom[N](n: N)(implicit orderN: Order[N], ringN: Ring[N]): LazyList[N] =
    LazyList.cons(n, lazyListsFrom(ringN.plus(n, ringN.one)))


  /**
   * gaps
   *
   * assumes that the input xs are already sorted
   */

  def gaps[T](xs: Seq[T])(implicit ringT: Ring[T]): Seq[(T, T)] = {
    import ringT.one
    xs.zip(xs.drop(1))
      .map({ case (x, y) => (x, y - x) })
      .filterNot(_._2 == one)
      .map({ case (x, g) => (x + one, x + g - one) })
  }

  def runs[T](xs: Seq[T], breaks: Set[T]): Seq[(T, T)] =
    if (xs.isEmpty) {
      List.empty
    } else {
      val tail = xs.drop(1)
      val run = tail.takeWhile(x => !breaks.contains(x))
      (xs.head, (xs.head +: run).last) +: runs(tail.drop(run.length), breaks)
    }

  /**
   * runs
   *
   * assumes xs is already sorted
   */

  def runs[T](xs: Seq[T])(implicit ringT: Ring[T], orderT: Order[T]): Seq[(T, T)] = {
    import ringT.one
    val breaks = xs.zip(xs.drop(1)).filter({ case (x, y) => orderT.compare(y - x, one) == 1 }).map(_._2).toSet
    runs(xs, breaks)
  }

  // List methods

  def replicate[T](n: Int)(v: T): List[T] = (0 until n).map(i => v).toList

  def reverse[T](l: List[T]): List[T] = l.reverse

  def intersperse[T](d: T)(l: List[T]): List[T] =
    (0 until (2 * l.size - 1)).map(i => i % 2 match { case 0 => l(i / 2) case 1 => d }).toList

  implicit def wrappedStringSpace[N](
    implicit
    iscSpace: MetricSpace[IndexedSeq[Char], N]): MetricSpace[String, N] =
      (s1: String, s2: String) => iscSpace.distance(s1, s2)

  implicit val rationalDoubleMetricSpace: MetricSpace[Rational, Double] =
    (v: Rational, w: Rational) =>
      spire.math.abs(v.toDouble - w.toDouble)

  implicit val realDoubleMetricSpace: MetricSpace[Real, Double] =
    (v: Real, w: Real) =>
      spire.math.abs(v.toDouble - w.toDouble)

  // implicit val doubleDoubleMetricSpace: MetricSpace[Double, Double] =
  //   (v: Double, w: Double) =>
  //     (v - w).abs

  implicit def metricSpaceFromAdditiveGroupSigned[N: AdditiveGroup: Signed]: MetricSpace[N, N] =
    (v: N, w: N) =>
      spire.math.abs(v - w)

  //import spire.math._

  //  implicit val rationalRng: Rng[Rational] = new Rng[Rational] {
  //
  //    val rat = new spire.math.RationalAlgebra()
  //
  //    def negate(x: Rational): Rational = rat.negate(x)
  //
  //    def zero: Rational = rat.zero
  //
  //    def plus(x: Rational, y: Rational): Rational = rat.plus(x, y)
  //
  //    def times(x: Rational, y: Rational): Rational = rat.times(x, y)
  //  }

  implicit def tuple2Field[V1, V2](implicit fieldV1: Field[V1], fieldV2: Field[V2], eqV1: cats.kernel.Eq[V1], eqV2: cats.kernel.Eq[V2]): Field[(V1, V2)] =
  new Field[(V1, V2)] {

    // Members declared in algebra.ring.AdditiveGroup
    def negate(x: (V1, V2)): (V1, V2) =
      (fieldV1.negate(x._1), fieldV2.negate(x._2))
  
    // Members declared in algebra.ring.AdditiveMonoid
    def zero: (V1, V2) =
      (fieldV1.zero, fieldV2.zero)
  
    // Members declared in algebra.ring.AdditiveSemigroup
    def plus(x: (V1, V2),y: (V1, V2)): (V1, V2) =
      (fieldV1.plus(x._1, y._1), fieldV2.plus(x._2, y._2))
  
    // Members declared in spire.algebra.GCDRing
    def gcd(a: (V1, V2),b: (V1, V2))(implicit ev: spire.algebra.Eq[(V1, V2)]): (V1, V2) =
      (fieldV1.gcd(a._1, b._1), fieldV2.gcd(a._2, b._2))

    def lcm(a: (V1, V2),b: (V1, V2))(implicit ev: spire.algebra.Eq[(V1, V2)]): (V1, V2) =
      (fieldV1.lcm(a._1, b._1), fieldV2.lcm(a._2, b._2))
  
    // Members declared in algebra.ring.MultiplicativeGroup
    def div(x: (V1, V2),y: (V1, V2)): (V1, V2) =
      (fieldV1.div(x._1, y._1), fieldV2.div(x._2, y._2))

    // Members declared in algebra.ring.MultiplicativeSemigroup
    def times(x: (V1, V2),y: (V1, V2)): (V1, V2) =
      (fieldV1.times(x._1, y._1), fieldV2.times(x._2, y._2))

    // MultiplicativeMonoid
    def one: (V1, V2) =
      (fieldV1.one, fieldV2.one)

  }

  object modules {

    val rat = new spire.math.RationalAlgebra()
    val realAlgebra = new spire.math.RealAlgebra

    implicit val fieldDouble: Field[Double] = spire.implicits.DoubleAlgebra
    implicit val ringInt: CRing[Int] = spire.implicits.IntAlgebra
    implicit val fieldFloat: Field[Float] = spire.implicits.FloatAlgebra

    implicit val doubleIntModule: CModule[Double, Int] =
      new CModule[Double, Int] {

        def negate(x: Double): Double = fieldDouble.negate(x)

        def zero: Double = fieldDouble.zero

        def plus(x: Double, y: Double): Double = fieldDouble.plus(x, y)

        implicit def scalar: CRing[Int] = ringInt

        def timesl(s: Int, v: Double): Double = s * v
      }

    implicit val doubleDoubleModule: CModule[Double, Double] =
      new CModule[Double, Double] {

        def negate(x: Double): Double = fieldDouble.negate(x)

        def zero: Double = fieldDouble.zero

        def plus(x: Double, y: Double): Double = fieldDouble.plus(x, y)

        implicit def scalar: CRing[Double] = fieldDouble

        def timesl(s: Double, v: Double): Double = s * v

      }

    implicit val realDoubleModule: CModule[Real, Double] =
      new CModule[Real, Double] {

        def negate(x: Real): Real = realAlgebra.negate(x)

        def zero: Real = Real(0)

        def plus(x: Real, y: Real): Real = x + y

        implicit def scalar: CRing[Double] = fieldDouble

        def timesl(s: Double, v: Real): Real = s * v
      }

    implicit val realRationalModule: CModule[Real, Rational] =
      new CModule[Real, Rational] {

        def negate(x: Real): Real = realAlgebra.negate(x)

        def zero: Real = Real(0)

        def plus(x: Real, y: Real): Real = x + y

        implicit def scalar: CRing[Rational] = rat

        def timesl(s: Rational, v: Real): Real = s * v
      }

    implicit val doubleRationalModule: CModule[Double, Rational] =
      new CModule[Double, Rational] {

        def negate(x: Double): Double = fieldDouble.negate(x)

        def zero: Double = fieldDouble.zero

        def plus(x: Double, y: Double): Double = fieldDouble.plus(x, y)

        implicit def scalar: CRing[Rational] = rat

        def timesl(s: Rational, v: Double): Double = s.toDouble * v

    }

    implicit val floatRationalModule: CModule[Float, Rational] =
      new CModule[Float, Rational] {

        def negate(x: Float): Float = fieldFloat.negate(x)

        def zero: Float = fieldFloat.zero

        def plus(x: Float, y: Float): Float = fieldFloat.plus(x, y)

        implicit def scalar: CRing[Rational] = rat

        def timesl(s: Rational, v: Float): Float = s.toDouble.toFloat * v

    }

    implicit val floatDoubleModule: CModule[Float, Double] =
      new CModule[Float, Double] {

        def negate(x: Float): Float = fieldFloat.negate(x)

        def zero: Float = fieldFloat.zero

        def plus(x: Float, y: Float): Float = fieldFloat.plus(x, y)

        implicit def scalar: CRing[Double] = fieldDouble

        def timesl(s: Double, v: Float): Float = (s * v).toFloat

      }

    implicit val rationalDoubleModule: CModule[Rational, Double] =
      new CModule[Rational, Double] {

        def negate(x: Rational): Rational = rat.negate(x)

        def zero: Rational = rat.zero

        def plus(x: Rational, y: Rational): Rational = rat.plus(x, y)

        implicit def scalar: CRing[Double] = fieldDouble

        def timesl(s: Double, v: Rational): Rational = s * v

    }

    implicit val rationalRationalModule: CModule[Rational, Rational] =
      new CModule[Rational, Rational] {

        def negate(x: Rational): Rational = rat.negate(x)

        def zero: Rational = rat.zero

        def plus(x: Rational, y: Rational): Rational = rat.plus(x, y)

        implicit def scalar: CRing[Rational] = rat

        def timesl(s: Rational, v: Rational): Rational = s * v

    }

  }
}
