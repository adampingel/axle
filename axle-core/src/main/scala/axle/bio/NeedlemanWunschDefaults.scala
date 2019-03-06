package axle.bio

object NeedlemanWunschDefaults {

  /**
   * similarity function for nucleotides
   *
   * S(a, b) === S(b, a)
   *
   */

  def similarity(x: Char, y: Char): Double = {
    val result: Int = (x, y) match {
      case ('A', 'A') => 10
      case ('A', 'G') => -1
      case ('A', 'C') => -3
      case ('A', 'T') => -4
      case ('G', 'A') => -1
      case ('G', 'G') => 7
      case ('G', 'C') => -5
      case ('G', 'T') => -3
      case ('C', 'A') => -3
      case ('C', 'G') => -5
      case ('C', 'C') => 9
      case ('C', 'T') => 0
      case ('T', 'A') => -4
      case ('T', 'G') => -3
      case ('T', 'C') => 0
      case ('T', 'T') => 8
      case _          => 0 // TODO not reachable, but scalac doesn't know that
    }
    result.toDouble
  }

  val gap = '-'

  val gapPenalty = -5d

}
