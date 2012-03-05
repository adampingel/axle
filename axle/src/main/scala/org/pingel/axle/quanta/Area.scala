package org.pingel.axle.quanta

import java.math.BigDecimal

class Area extends Quantum {

  type UOM = AreaUnit

  class AreaUnit(
    baseUnit: Option[UOM] = None,
    magnitude: BigDecimal,
    name: Option[String] = None,
    symbol: Option[String] = None,
    link: Option[String] = None)
    extends UnitOfMeasurement(baseUnit, magnitude, name, symbol, link)
  
}


object Area extends Quantum {

  import Distance.{meter}
  
  val wikipediaUrl = "http://en.wikipedia.org/wiki/Area"
    
  val derivations = List(Distance squared)

  val m2 = meter squared
  
}