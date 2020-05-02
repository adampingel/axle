package axle.awt

import java.awt.image.BufferedImage

import axle.visualize.Color
import axle.visualize.PixelatedColoredArea
import scala.annotation.implicitNotFound

@implicitNotFound("Witness not found for Image[${T}]")
trait Image[T] {

  def image(t: T): BufferedImage

}

object Image {

  final def apply[T](implicit i: Image[T]): Image[T] = i

  /**
   * http://stackoverflow.com/questions/4028898/create-an-image-from-a-non-visible-awt-component
   */

  implicit def draw2image[T: Draw]: Image[T] = new Image[T] {

    def image(t: T): BufferedImage = {
      val component = Draw[T].component(t)

      val minSize = component.getMinimumSize
      val frame = AxleFrame(minSize.width, minSize.height)
      frame.setUndecorated(true)
      frame.initialize()
      frame.add(component) // returns Component
      // rc.setVisible(true)
      frame.setVisible(true)

      val image = new BufferedImage(frame.getWidth, frame.getHeight, BufferedImage.TYPE_INT_RGB)
      val g = image.createGraphics()
      frame.paintAll(g)

      g.dispose()
      frame.setVisible(false)
      frame.dispose

      image
    }
  }

  implicit def pcaImage[X, Y, V]: Image[PixelatedColoredArea[X, Y, V]] =
    new Image[PixelatedColoredArea[X, Y, V]] {

      def image(pca: PixelatedColoredArea[X, Y, V]): BufferedImage = {
        import pca._
        val lastRow = height - 1
        val image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        (0 until width) foreach { column =>
          val x0 = scaledArea.unframeX(column.toDouble)
          val x1 = scaledArea.unframeX(column + 1d)
          (0 until height) foreach { row =>
            val y0 = scaledArea.unframeY(row.toDouble)
            val y1 = scaledArea.unframeY(row + 1d)
            val rgb = Color.toRGB(c(f(x0, x1, y0, y1)))
            image.setRGB(column, lastRow - row, rgb)
          }
        }

        image
      }
    }

}
