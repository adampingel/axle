package axle

trait MapReduce {

  import collection._

  def mapReduce[D, K, V](data: Iterator[D], mapper: D => Seq[(K, V)], reducer: (V, V) => V): immutable.Map[K, V]

}

object ScalaMapReduce extends MapReduce {

  import collection._

  def mapReduce[D, K, V](data: Iterator[D], mapper: D => Seq[(K, V)], reducer: (V, V) => V): immutable.Map[K, V] =
    data
      .flatMap(mapper(_))
      .toList // TODO inefficient
      .groupBy(_._1)
      .map({ case (k, v) => (k, v.map(_._2).reduce(reducer)) })

}
