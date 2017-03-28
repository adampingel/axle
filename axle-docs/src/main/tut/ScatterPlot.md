---
layout: page
title: ScatterPlot
permalink: /tutorial/scatterplot/
---

ScatterPlot


```tut:book
import axle.visualize._

val data = Map(
  (1, 1) -> 0,
  (2, 2) -> 0,
  (3, 3) -> 0,
  (2, 1) -> 1,
  (3, 2) -> 1,
  (0, 1) -> 2,
  (0, 2) -> 2,
  (1, 3) -> 2)
```

Define the coloring strategy:

```tut
import axle.visualize.Color._

val colorer =
  (d: Map[(Int, Int), Int], x: Int, y: Int) => d((x, y)) match {
    case 0 => red
    case 1 => blue
    case 2 => green
  }
```

Define the labeling strategy:

```tut
val labeller =
  (d: Map[(Int, Int), Int], x: Int, y: Int) => d.get((x, y)).map(s => (s.toString, true))
```

Define the ScatterPlot

```tut
import cats.implicits._

val plot = ScatterPlot[String, Int, Int, Map[(Int, Int), Int]](data, colorOf = colorer, labelOf = labeller)
```

Create the SVG

```tut
import axle.web._

svg(plot, "scatter.svg")
```

![scatter](/tutorial/images/scatter.svg)