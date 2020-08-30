package axle

import scala.Stream.cons

import cats.implicits._

case class CrossProduct[E](collections: IndexedSeq[IndexedSeq[E]])
  extends Iterable[List[E]] {

  def iterator: Iterator[List[E]] = result.toIterator

  lazy val result: Stream[List[E]] = tail(collections.map(c => -1), 0)

  def current(indices: IndexedSeq[Int]): List[E] = collections.zip(indices).map({ case (c, i) => c(i) }).toList

  def tail(indices0: IndexedSeq[Int], i0: Int): Stream[List[E]] =
    if (i0 === collections.size) {
      Stream.empty
    } else {
      if (indices0(i0) === -1) {
        if (collections(i0).size > 0) {
          val indices1 = indices0.updated(i0, 0)
          if (i0 < collections.size - 1) {
            tail(indices1, i0 + 1)
          } else {
            cons(current(indices1), tail(indices1, 0))
          }
        } else {
          Stream.empty
        }
      } else if (indices0(i0) + 1 < collections(i0).size) {
        val indices1 = indices0.updated(i0, indices0(i0) + 1)
        cons(current(indices1), tail(indices1, 0))
      } else {
        tail(indices0.updated(i0, 0), i0 + 1)
      }
    }

}
