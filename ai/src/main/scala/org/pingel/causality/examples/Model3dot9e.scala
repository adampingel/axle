
package org.pingel.causality.examples

import org.pingel.causality.CausalModel
import org.pingel.causality.Function
import org.pingel.bayes.ModelVisualizer
import org.pingel.bayes.RandomVariable

object Model3dot9e extends CausalModel("3.9e") {

        val X = addVariable(new RandomVariable("X"))
		val Y = addVariable(new RandomVariable("Y"))
		val Z = addVariable(new RandomVariable("Z"))
		val U1 = addVariable(new RandomVariable("U1", None, false))
		val U2 = addVariable(new RandomVariable("U2", None, false))

		addFunction(new Function(X, List(Z, U1)))
		addFunction(new Function(Y, List(X, Z, U2)))
		addFunction(new Function(Z, List(U1, U2)))

  def main(args: Array[String]) {
    ModelVisualizer.draw(Model3dot9e)
  }

}
