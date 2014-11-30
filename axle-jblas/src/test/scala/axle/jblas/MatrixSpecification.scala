package axle.jblas

import org.specs2.mutable._
import axle.jblas.ConvertedJblasDoubleMatrix.jblasConvertedMatrix

class MatrixSpecification extends Specification {

  val witness = jblasConvertedMatrix
  import witness._

  "DoubleJblasMatrix" should {
    "work" in {

      val z = zeros[Double](3, 4)
      val o = ones[Double](2, 3)
      val r = rand[Double](1, 2)
      val rn = randn[Double](2, 2)

      val dm = rand[Double](3, 3)
      val c2 = witness.column(dm)(2)
      val r2 = witness.row(dm)(2)

      1 must be equalTo (1)
    }
  }

  "IntJblasMatrix" should {
    "work" in {

      val z = zeros[Int](1, 3)
      val o = ones[Int](2, 2)
      val e = eye[Int](3)

      1 must be equalTo (1)
    }
  }

  "BooleanJblasMatrix" should {
    "work" in {

      val f = falses(2, 3)
      val t = trues(3, 2)
      val e = eye[Boolean](4)

      1 must be equalTo (1)
    }
  }

  "SetMatrix" should {
    "work" in {

      // SetMatrixFactory.zeros[Int](3, 3)

      1 must be equalTo (1)
    }
  }

}
