package org.pingel.bayes

import org.pingel.util.ListCrossProduct

//import scalala.tensor.mutable._
//import scalala.tensor.dense._
import org.pingel.util.Matrix
import scala.collection._

object Factor {

  def multiply(tables: Collection[Factor]): Factor = {
		
    if( tables.size == 0 ) {
      return null
    }
		
    // TODO this can be made more efficient by constructing a single
    // result table ahead of time.
		
    val it = tables.iterator
    var current = it.next()
    while( it.hasNext ) {
      current = current.multiply(it.next())
    }
    current
  }

}

/* Technically a "Distribution" is probably a table that sums to 1, which is not
 * always true in a Factor.  They should be siblings rather than parent/child.
 */

class Factor(varList: List[RandomVariable]) extends Distribution(varList)
{
  var elements: Array[Double]

  var cp: ListCrossProduct[Value] = null

  makeCrossProduct()

  var name = "unnamed"
	
  def setName(name: String): Unit = { this.name = name; }

  def getName(): String = name

  def getLabel(): String = name
	
  def makeCrossProduct(): Unit = {
    val valLists: List[List[Value]] = varList.map( v => v.getDomain.getValues )
    cp = new ListCrossProduct[Value](valLists)
    elements = new Array[Double](cp.size)
  }
	
  def evaluate(prior: Case, condition: Case): Double = {
    // assume prior and condition are disjoint, and that they are
    // each compatible with this table
		
    var w = 0.0
    var p = 0.0
    for( i <- 0 until numCases ) {
      val c = caseOf(i)
      if( c.isSupersetOf(prior) ) {
    	  w += read(c)
    	  if( c.isSupersetOf(condition) ) {
             p += read(c)
    	  }
      }
    }
		
    p / w
  }
	
  def indexOf(c: Case): Int = {
    var objects = c.valuesOf(varList)
    cp.indexOf(objects)
  }
	
  def caseOf(i: Integer): Case = {
    var result = new Case()
    val values = cp(i)
    result.assign(varList, values)
    result
  }
	
  def numCases(): Int = elements.length
	
  def write(c: Case, d: Double): Unit = {
//		System.out.println("write: case = " + c.toOrderedString(variables) + ", d = " + d);
//		System.out.println("variables.length = " + variables.length);
    elements(indexOf(c)) = d
  }
	

  def read(c: Case): Double = elements(indexOf(c))
  
	
  def print(): Unit = {
    for( i <- 0 until elements.length ) {
      val c = caseOf(i)
      println(c.toOrderedString(varList) + " " + read(c))
    }
  }
	
  def maxOut(variable: RandomVariable): Factor = {
    // Chapter 6 definition 6

    var vars = getVariables.filter( v => ! variable.equals(v) )

    var newFactor = new Factor(vars)
    for( i <- 0 until newFactor.numCases() ) {
      def ci = newFactor.caseOf(i)
      var bestValue: Value = null
      var maxSoFar = Double.MinValue
      for( value <- variable.getDomain().getValues()) {
    	  var cj = newFactor.caseOf(i)
    	  cj.assign(variable, value)
          val s = this.read(cj)
          if( bestValue == null ) {
             maxSoFar = s
             bestValue = value
          }
          else {
             if( s > maxSoFar ) {
                maxSoFar = s
                bestValue = value
             }
          }
      }
      newFactor.write(ci, maxSoFar)
    }
    newFactor
  }
  
  def projectToOnly(remainingVars: List[RandomVariable]): Factor = {
    var result = new Factor(remainingVars)
    
    for( j <- 0 until numCases ) {
      var fromCase = this.caseOf(j)
      var toCase = fromCase.projectToVars(remainingVars)
      val additional = this.read(fromCase)
      val previous = result.read(toCase)
      result.write(toCase, previous + additional)
    }
    result
  }
	
  def tally(a: RandomVariable, b: RandomVariable): Matrix[Double] = {
    val aValues = a.getDomain.getValues
    val bValues = b.getDomain.getValues
		
    var tally = Matrix.zeros[Double](aValues.size, bValues.size)
    var w = new Case()
    var r = 0
    for( aVal <- aValues ) {
      w.assign(a, aVal)
      var c = 0
      for( bVal <- bValues ) {
    	  w.assign(b, bVal)
    	  for( j <- 0 until numCases ) {
    		  val m = this.caseOf(j)
    		  if( m.isSupersetOf(w) ) {
    			  tally(r, c) += this.read(m)
    		  }
    	  }
    	  c += 1
      }
      r += 1
    }
    return tally;
  }
  
  def sumOut(varToSumOut: RandomVariable): Factor = {
    // depending on assumptions, this may not be the best way to remove the vars
    
    var newVars = mutable.ListBuffer[RandomVariable]()
    for( x <- getVariables() ) {
      if( x.compareTo(varToSumOut) != 0 ) {
    	  newVars += x
      }
    }
		
    var result = new Factor(newVars.toList)
    for( j <- 0 until result.numCases() ) {
      var c = result.caseOf(j)
      var p = 0.0
      for( value <- varToSumOut.getDomain.getValues ) {
    	  var f = c.copy()
    	  f.assign(varToSumOut, value)
    	  p += read(f)
      }
      result.write(c, p)
    }
    
    result
  }

  def sumOut(varsToSumOut: Set[RandomVariable]): Factor = {
    // TODO not the most efficient way to sum out a set of variables
		
    var result = this
    varsToSumOut.map( v => {
      result = result.sumOut(v)
    })
    result
  }
	
  def projectRowsConsistentWith(e: Case): Factor = {
		
    // as defined on chapter 6 page 15
		
    var result = new Factor(getVariables())
		
    for( j <- 0 until result.numCases ) {
      var c = result.caseOf(j)
      if( c.isSupersetOf(e) ) {
    	  result.elements(j) = elements(j)
      }
      else {
    	  result.elements(j) = 0.0
      }
    }
    
    result
  }
	
  def multiply(other: Factor): Factor = {
		
    var newVarList = mutable.ListBuffer[RandomVariable]()
    newVarList ++= getVariables()

    var myVarsAsSet = Set[RandomVariable]()
    myVarsAsSet ++= getVariables()

    for( x <- other.getVariables() ) {
      if( ! myVarsAsSet.contains(x) ) {
    	  newVarList += x
      }
    }
    
    var result = new Factor(newVarList.toList)
    
    for(j <- 0 until result.numCases() ) {
      var c = result.caseOf(j)
      val myContribution = this.read(c)
      val otherContribution = other.read(c)
      result.write(c, myContribution * otherContribution)
    }
    
    result
  }
  
  def mentions(variable: RandomVariable) = getVariables.exists( v => variable.getName.equals(v.getName) ) 
	
}
