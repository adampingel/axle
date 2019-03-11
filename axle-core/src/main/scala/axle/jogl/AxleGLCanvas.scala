package axle.jogl

import java.net.URL

import scala.Vector

import com.jogamp.opengl.GL.GL_COLOR_BUFFER_BIT
import com.jogamp.opengl.GL.GL_DEPTH_BUFFER_BIT
import com.jogamp.opengl.GL.GL_DEPTH_TEST
import com.jogamp.opengl.GL.GL_LEQUAL
import com.jogamp.opengl.GL.GL_LINEAR
import com.jogamp.opengl.GL.GL_NICEST
import com.jogamp.opengl.GL.GL_TEXTURE_2D
import com.jogamp.opengl.GL.GL_TEXTURE_MAG_FILTER
import com.jogamp.opengl.GL.GL_TEXTURE_MIN_FILTER
import com.jogamp.opengl.GL2ES1.GL_PERSPECTIVE_CORRECTION_HINT
import com.jogamp.opengl.GLAutoDrawable
import com.jogamp.opengl.GLEventListener
import com.jogamp.opengl.awt.GLCanvas
import com.jogamp.opengl.fixedfunc.GLLightingFunc.GL_AMBIENT
import com.jogamp.opengl.fixedfunc.GLLightingFunc.GL_COLOR_MATERIAL
import com.jogamp.opengl.fixedfunc.GLLightingFunc.GL_DIFFUSE
import com.jogamp.opengl.fixedfunc.GLLightingFunc.GL_LIGHT0
import com.jogamp.opengl.fixedfunc.GLLightingFunc.GL_LIGHTING
import com.jogamp.opengl.fixedfunc.GLLightingFunc.GL_POSITION
import com.jogamp.opengl.fixedfunc.GLLightingFunc.GL_SMOOTH
import com.jogamp.opengl.fixedfunc.GLLightingFunc.GL_SPECULAR
import com.jogamp.opengl.fixedfunc.GLMatrixFunc.GL_MODELVIEW
import com.jogamp.opengl.fixedfunc.GLMatrixFunc.GL_PROJECTION
import com.jogamp.opengl.glu.GLU

import com.jogamp.opengl.util.texture.Texture
import com.jogamp.opengl.util.texture.TextureIO

import axle.quanta.Angle
import axle.quanta.AngleConverter
import axle.quanta.Distance
import axle.quanta.DistanceConverter
import axle.quanta.UnitOfMeasurement
import axle.quanta.UnittedQuantity

case class AxleGLCanvas[S](
  scene:        SceneFrame[S],
  textureUrls:  Seq[(URL, String)],
  fovy:         UnittedQuantity[Angle, Float],
  zNear:        UnittedQuantity[Distance, Float],
  zFar:         UnittedQuantity[Distance, Float],
  distanceUnit: UnitOfMeasurement[Distance])(
  implicit
  angleMeta:    AngleConverter[Float],
  distanceMeta: DistanceConverter[Float])
  extends GLCanvas with GLEventListener {

  this.addGLEventListener(this)

  var rc: RenderContext = null
  var state: S = scene.initialState

  override def init(drawable: GLAutoDrawable): Unit = {
    val gl = drawable.getGL.getGL2
    val glu = new GLU()
    gl.glClearColor(0f, 0f, 0f, 0f)
    gl.glClearDepth(1f)
    gl.glEnable(GL_DEPTH_TEST)
    gl.glDepthFunc(GL_LEQUAL)
    gl.glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST)
    gl.glShadeModel(GL_SMOOTH)

    val url2texture: Map[URL, Texture] =
      textureUrls map {
        case (url, extension) =>
          url -> TextureIO.newTexture(url, false, extension)
      } toMap

    rc = RenderContext(url2texture, glu)

    gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)
    gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)

    val lightAmbientValue = Vector(0.2f, 0.2f, 0.2f, 1f).toArray
    val lightSpecular = Vector(0.2f, 0.2f, 0.2f, 1f).toArray
    val lightDiffuseValue = Vector(1f, 1f, 1f, 1f).toArray
    val lightDiffusePosition = Vector(5f, 1f, 10f, 1f).toArray

    gl.glEnable(GL_LIGHTING)
    gl.glEnable(GL_LIGHT0)
    gl.glLightfv(GL_LIGHT0, GL_AMBIENT, lightAmbientValue, 0)
    gl.glLightfv(GL_LIGHT0, GL_SPECULAR, lightSpecular, 0)
    gl.glLightfv(GL_LIGHT0, GL_DIFFUSE, lightDiffuseValue, 0)
    gl.glLightfv(GL_LIGHT0, GL_POSITION, lightDiffusePosition, 0)

    gl.glEnable(GL_COLOR_MATERIAL)
  }

  override def reshape(drawable: GLAutoDrawable, x: Int, y: Int, width: Int, height: Int): Unit = {
    val gl = drawable.getGL.getGL2

    assert(height > 0)
    val aspect = width.toFloat / height
    gl.glViewport(0, 0, width, height)
    gl.glMatrixMode(GL_PROJECTION)
    gl.glLoadIdentity()
    fovy.in(angleMeta.degree)
    rc.glu.gluPerspective(
      (fovy in angleMeta.degree).magnitude,
      aspect,
      (zNear in distanceUnit).magnitude,
      (zFar in distanceUnit).magnitude)
    gl.glMatrixMode(GL_MODELVIEW)
    gl.glLoadIdentity()
  }

  def display(drawable: GLAutoDrawable): Unit = {

    val gl = drawable.getGL.getGL2
    gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT)

    scene.renderAll(gl, rc, state)
    state = scene.tic(state)
  }

  override def dispose(drawable: GLAutoDrawable): Unit = {}
}
