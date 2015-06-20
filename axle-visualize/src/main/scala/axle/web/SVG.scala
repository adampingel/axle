package axle.web

import scala.annotation.implicitNotFound
import scala.xml.NodeSeq
import scala.xml.NodeSeq.seqToNodeSeq

import java.awt.Font
import java.awt.Color

import axle.algebra.LengthSpace
import axle.algebra.Tics
import axle.algebra.Zero

import axle.visualize.Point2D
import axle.visualize.Plot
import axle.visualize.PlotView
import axle.visualize.angleDouble
import axle.visualize.element.DataLines
import axle.visualize.element.HorizontalLine
import axle.visualize.element.Key
import axle.visualize.element.Text
import axle.visualize.element.VerticalLine
import axle.visualize.element.XTics
import axle.visualize.element.YTics
import spire.algebra.Eq

@implicitNotFound("Witness not found for SVG[${S}]")
trait SVG[S] {

  def svg(s: S): NodeSeq

}

object SVG {

  @inline final def apply[S: SVG]: SVG[S] = implicitly[SVG[S]]

  def rgb(color: Color): String = s"rgb(${color.getRed},${color.getGreen},${color.getBlue})"

  implicit def svgDataLines[X, Y, D]: SVG[DataLines[X, Y, D]] =
    new SVG[DataLines[X, Y, D]] {
      def svg(dl: DataLines[X, Y, D]): NodeSeq = {

        import dl._

        val pointRadius = pointDiameter / 2

        data.zip(colorStream).flatMap {
          case ((_, d), color) => {
            val xs = orderedXs(d).toList
            val centers = xs.map(x => scaledArea.framePoint(Point2D(x, x2y(d, x))))
            val points = (centers map { c => s"${c.x},${c.y}" }).mkString(" ")
            val polyline = <polyline points={ s"$points" } fill="none" stroke={ s"${rgb(color)}" } stroke-width="1"/>
            val pointCircles =
              if (pointRadius > 0) {
                centers map { c =>
                  <circle cx={ s"${c.x}" } cy={ s"${c.y}" } r={ s"${pointRadius}" } fill={ s"${rgb(color)}" }/>
                }
              } else {
                List.empty
              }
            polyline :: pointCircles
          }
        }
      }
    }

  implicit def svgKey[X, Y, D]: SVG[Key[X, Y, D]] =
    new SVG[Key[X, Y, D]] {
      def svg(key: Key[X, Y, D]): NodeSeq = {
        import key._
        data.zip(colorStream).zipWithIndex map {
          case (((label, _), color), i) => {
            <text x={ s"${plot.width - width}" } y={ s"${topPadding + font.getSize * (i + 1)}" } fill={ s"rgb(${color.getRed},${color.getGreen},${color.getBlue})" } font-size={ s"${font.getSize}" }>{ label }</text>
          }
        }
      }
    }

  implicit def svgText: SVG[Text] =
    new SVG[Text] {

      def svg(t: Text): NodeSeq = {

        import t._

        if (angle.isDefined) {
          import axle.visualize.angleDouble
          import spire.implicits._
          val twist = angle.get.in(angleDouble.degree).magnitude * -1d
          if (centered) {
            <text text-anchor="middle" x={ s"$x" } y={ s"$y" } transform={ s"rotate($twist $x $y)" } fill={ s"rgb(${color.getRed},${color.getGreen},${color.getBlue})" } font-size={ s"${font.getSize}" }>{ t.text }</text>
          } else {
            <text text-anchor="left" x={ s"$x" } y={ s"$y" } transform={ s"rotate($twist $x $y)" } fill={ s"rgb(${color.getRed},${color.getGreen},${color.getBlue})" } font-size={ s"${font.getSize}" }>{ t.text }</text>
          }
        } else {
          if (centered) {
            <text text-anchor="middle" x={ s"$x" } y={ s"$y" } fill={ s"rgb(${color.getRed},${color.getGreen},${color.getBlue})" } font-size={ s"${font.getSize}" }>{ t.text }</text>
          } else {
            <text text-anchor="left" x={ s"$x" } y={ s"$y" } fill={ s"rgb(${color.getRed},${color.getGreen},${color.getBlue})" } font-size={ s"${font.getSize}" }>{ t.text }</text>
          }
        }
      }
    }

  implicit def svgHorizontalLine[X, Y]: SVG[HorizontalLine[X, Y]] =
    new SVG[HorizontalLine[X, Y]] {

      def svg(hl: HorizontalLine[X, Y]): NodeSeq = {
        import hl._
        val p0 = Point2D(scaledArea.minX, h)
        val p1 = Point2D(scaledArea.maxX, h)
        val fp0 = scaledArea.framePoint(p0)
        val fp1 = scaledArea.framePoint(p1)
        <line x1={ s"${fp0.x}" } y1={ s"${fp0.y}" } x2={ s"${fp1.x}" } y2={ s"${fp1.y}" } stroke={ s"${rgb(color)}" } stroke-width="1"/>
      }
    }

  implicit def svgVerticalLine[X, Y]: SVG[VerticalLine[X, Y]] =
    new SVG[VerticalLine[X, Y]] {

      def svg(vl: VerticalLine[X, Y]): NodeSeq = {
        import vl._
        val p0 = Point2D(v, scaledArea.minY)
        val p1 = Point2D(v, scaledArea.maxY)
        val fp0 = scaledArea.framePoint(p0)
        val fp1 = scaledArea.framePoint(p1)
        <line x1={ s"${fp0.x}" } y1={ s"${fp0.y}" } x2={ s"${fp1.x}" } y2={ s"${fp1.y}" } stroke={ s"${rgb(color)}" } stroke-width="1"/>
      }
    }

  implicit def svgPlot[X: Zero: Tics: Eq, Y: Zero: Tics: Eq, D](
    implicit xls: LengthSpace[X, _], yls: LengthSpace[Y, _]): SVG[Plot[X, Y, D]] = new SVG[Plot[X, Y, D]] {

    def svg(plot: Plot[X, Y, D]): NodeSeq = {

      import plot._

      val normalFont = new Font(fontName, Font.BOLD, fontSize)
      val xAxisLabelText = xAxisLabel.map(Text(_, normalFont, width / 2, height - border / 2))
      val yAxisLabelText = yAxisLabel.map(Text(_, normalFont, 20, height / 2, angle = Some(90d *: angleDouble.degree)))
      val titleFont = new Font(titleFontName, Font.BOLD, titleFontSize)
      val titleText = title.map(Text(_, titleFont, width / 2, titleFontSize))

      val view = PlotView(plot, plot.initialValue, normalFont)

      import view._

      val nodes =
        SVG[HorizontalLine[X, Y]].svg(hLine) ::
          SVG[VerticalLine[X, Y]].svg(vLine) ::
          SVG[XTics[X, Y]].svg(xTics) ::
          SVG[YTics[X, Y]].svg(yTics) ::
          SVG[DataLines[X, Y, D]].svg(dataLines) ::
          List(
            titleText.map(SVG[Text].svg),
            xAxisLabelText.map(SVG[Text].svg),
            yAxisLabelText.map(SVG[Text].svg),
            view.keyOpt.map(SVG[Key[X, Y, D]].svg)).flatten

      svgFrame(nodes.reduce(_ ++ _), width, height)
    }
  }

  implicit def svgXTics[X, Y]: SVG[XTics[X, Y]] =
    new SVG[XTics[X, Y]] {

      def svg(xt: XTics[X, Y]): NodeSeq = {

        import xt._
        import scaledArea._

        tics flatMap {
          case (x, label) => {
            val pre = if (fDrawLines) {
              val p0 = Point2D(x, minY)
              val p1 = Point2D(x, maxY)
              val fp0 = scaledArea.framePoint(p0)
              val fp1 = scaledArea.framePoint(p1)
              List(<line x1={ s"${fp0.x}" } y1={ s"${fp0.y}" } x2={ s"${fp1.x}" } y2={ s"${fp1.y}" } stroke={ s"${rgb(Color.lightGray)}" } stroke-width="1"/>)
            } else {
              Nil
            }
            val bottomScaled = Point2D(x, minY)
            val bottomUnscaled = framePoint(bottomScaled)
            // TODO: if (angle === zeroDegrees)
            pre ++ List(
              <text text-anchor="middle" alignment-baseline="hanging" x={ s"${bottomUnscaled.x}" } y={ s"${bottomUnscaled.y}" } fill={ s"rgb(${color.getRed},${color.getGreen},${color.getBlue})" } font-size={ s"${font.getSize}" }>{ label }</text>,
              <line x1={ s"${bottomUnscaled.x}" } y1={ s"${bottomUnscaled.y - 2}" } x2={ s"${bottomUnscaled.x}" } y2={ s"${bottomUnscaled.y + 2}" } stroke={ s"${rgb(Color.lightGray)}" } stroke-width="1"/>)
          }
        }
      }
    }

  implicit def svgYTics[X, Y]: SVG[YTics[X, Y]] =
    new SVG[YTics[X, Y]] {

      def svg(yt: YTics[X, Y]): NodeSeq = {

        import yt._
        import scaledArea._

        tics.flatMap({
          case (y, label) => {

            val leftScaled = Point2D(minX, y)
            val rightScaled = Point2D(maxX, y)
            val leftUnscaled = framePoint(leftScaled)
            val rightUnscaled = framePoint(rightScaled)

            List(
              <line x1={ s"${leftUnscaled.x}" } y1={ s"${leftUnscaled.y}" } x2={ s"${rightUnscaled.x}" } y2={ s"${rightUnscaled.y}" } stroke={ s"${rgb(Color.lightGray)}" } stroke-width="1"/>,
              <text text-anchor="end" alignment-baseline="middle" x={ s"${leftUnscaled.x - 5}" } y={ s"${leftUnscaled.y}" } font-size={ s"${font.getSize}" }>{ label }</text>,
              <line x1={ s"${leftUnscaled.x - 2}" } y1={ s"${leftUnscaled.y}" } x2={ s"${leftUnscaled.x + 2}" } y2={ s"${leftUnscaled.y}" } stroke={ s"${rgb(Color.lightGray)}" } stroke-width="1"/>)
          }
        })
      }
    }

}
