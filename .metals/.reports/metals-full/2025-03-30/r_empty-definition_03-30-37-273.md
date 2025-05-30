error id: scala/None.
file://<WORKSPACE>/workflows4s-core/src/main/scala/workflows4s/mermaid/mermaid.scala
empty definition using pc, found symbol in pc: scala/None.
empty definition using semanticdb
empty definition using fallback
non-local guesses:
	 -None.
	 -None#
	 -None().
	 -scala/Predef.None.
	 -scala/Predef.None#
	 -scala/Predef.None().
offset: 871
uri: file://<WORKSPACE>/workflows4s-core/src/main/scala/workflows4s/mermaid/mermaid.scala
text:
```scala
package workflows4s.mermaid

sealed trait MermaidElement {
  def render: String
}

case class ClassDef(name: String, style: String) extends MermaidElement {
  def render: String = s"classDef $name $style"
}

case class Node(id: String, label: String, shape: Option[String] = None, clazz: Option[String] = None) extends MermaidElement {
  def render: String = {
    val classSuffix = clazz.map(c => s":::${c}").getOrElse("")
    shape match {
      case Some(s) => s"""$id$classSuffix@{ shape: $s, label: "$label"}"""
      case None    => s"""$id["$label"]$classSuffix"""
    }
  }
}

case class Link(from: String, to: String, label: Option[String] = None, prefix: "" | "x" = "", suffix: ">" | "x" = ">", midfix: "." | "" = "")
    extends MermaidElement {
  def render: String = label match {
    case Some(l) => s"""$from $prefix-$midfix-$suffix|"$l"| $to"""
    case N@@one    => s"$from ${prefix}-${midfix}-${suffix} $to"
  }
}

case class Subgraph(id: String, name: String, elements: Seq[MermaidElement]) extends MermaidElement {
  def render: String =
    s"""subgraph $id ["$name"]
${elements.map(_.render).mkString("\n")}
end""".stripMargin
}

case class MermaidFlowchart(elements: Seq[MermaidElement]) {

  def addElement(element: MermaidElement): MermaidFlowchart  = MermaidFlowchart(elements :+ element)
  def addElements(el: Seq[MermaidElement]): MermaidFlowchart = MermaidFlowchart(elements ++ el)

  def render: String =
    s"""flowchart TD
${elements.map(_.render).mkString("\n")}
""".stripMargin
}

object MermaidFlowchart {
  def apply(): MermaidFlowchart = MermaidFlowchart(Seq.empty)
}

```


#### Short summary: 

empty definition using pc, found symbol in pc: scala/None.