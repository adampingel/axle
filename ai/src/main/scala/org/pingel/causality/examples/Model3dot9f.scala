
package org.pingel.causality.examples

import org.pingel.causality.CausalModel
import org.pingel.ptype.PFunction
import org.pingel.bayes.ModelVisualizer
import org.pingel.bayes.RandomVariable

object Model3dot9f extends CausalModel("3.9f") {

  val X = addVariable(new RandomVariable("X"))
  val Y = addVariable(new RandomVariable("Y"))
  val Z = addVariable(new RandomVariable("Z"))
  val U1 = addVariable(new RandomVariable("U1", None, false))
  val U2 = addVariable(new RandomVariable("U2", None, false))

  addFunction(new PFunction(X, List(U1)))
  addFunction(new PFunction(Z, List(X, U2)))
  addFunction(new PFunction(Y, List(Z, U1, U2)))

  def main(args: Array[String]) {
    ModelVisualizer.draw(Model3dot9f)
  }

}
