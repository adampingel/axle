package axle.slc

sealed trait Expression[T]

case class Variable[T](x: Symbol) extends Expression[T]

case class Value[T](v: T) extends Expression[T]

// E[_], T
case class λ[T](v: Symbol, e: Expression[T]) extends Expression[T]

case class Sum[T, V](e1: Expression[T], e2: Expression[V]) extends Expression[V]

case class Let[T, V](x: Symbol, ep: Expression[T], e: Expression[V]) extends Expression[V]

case class Choose[T](p: Expression[Boolean], e1: Expression[T], e2: Expression[T]) extends Expression[T]

case class Pair[T, V](e1: Expression[T], e2: Expression[V]) extends Expression[(T, V)]

case class First[T, V](e: Pair[T, V]) extends Expression[T]

case class Second[T, V](e: Pair[T, V]) extends Expression[V]

// case class LeftE[T, V](e1: S) extends Expression[L, R]
// case class RightE ... 

case class Case[E, L, R](e: E, el: L, er: R) extends Expression[Either[L, R]]

// Syntactic sugar for multiple Choose
case class Dist[N, T](m: (N, T)*) extends Expression[T]
