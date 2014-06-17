package axle.visualize

import java.awt.Graphics2D
import java.awt.Graphics
import javax.swing.JPanel
import java.awt.Color

class BackgroundPanel(title: String) extends JPanel {

  override def paintComponent(g: Graphics): Unit = {
    super.paintComponent(g)
    val g2d = g.asInstanceOf[Graphics2D]
    g2d.setColor(Color.black)
    g2d.drawString(title, 20, 20)
  }

}
