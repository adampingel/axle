package axle.slc

/**
 * @author pingel
 */
trait Probability[E[_], T] {

  def foo(e: E[T], ρ: Map[Symbol, Any]): T
}

object Probability {

  def apply[E[_], T](implicit p: Probability[E, T]) = p

  // P[x]ρ = return (ρ x)
  implicit def probabilityVariable[T] = new Probability[Variable, T] {

    def foo(e: Variable[T], ρ: Map[Symbol, Any]): T =
      ρ(e.x).asInstanceOf[T]
  }

  // P[v]ρ = return v
  implicit def probabilityValue[T] = new Probability[Value, T] {

    def foo(e: Value[T], ρ: Map[Symbol, Any]): T =
      e.v
  }

  // P[λx.e]ρ = return (λv.P[e]ρ{x 􏰀→ v})
  implicit def probabilityλ[T] = new Probability[λ, T] {

    def foo(e: λ[T], ρ: Map[Symbol, Any]): T = {
      Probability[Expression, T].foo(e.e, p + x -> v)
      ??? // return (λv.P[e]ρ{x 􏰀→ v})
    }
  }

  // P[let x=e′ in e]ρ = P[e′]ρ>>=λv.P[e]ρ{x􏰀→v}
  // P[e1 e2]ρ = P[e1]ρ >>= λv1.P[e2]ρ >>= λv2.v1v2
  // P[(e1 , e2 )]ρ = P[e1]ρ >>= λv1.P[e2]ρ >>= λv2.return (v1, v2)
  // P[e.1]ρ = P [e]ρ >>= (return ◦ fst)
  // P[e.2]ρ = P [e]ρ >>= (return ◦ snd)
  // P[choose p e1 e2]ρ = choose p (P[e1]ρ) (P[e2]ρ)
  // P[L e]ρ = P [e]ρ >>= (return ◦ Left)
  // P[R e]ρ = P [e]ρ >>= (return ◦ Right)
  // P[case e el er]ρ = P[e]ρ >>= either (λv.P[el]ρ >>= λf.f v) (λv.P[er]ρ >>= λf.f v)

}