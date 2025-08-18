package workflows4s.web.ui.components

import tyrian.Html
import tyrian.Html.*
import workflows4s.web.ui.Msg
import scala.util.matching.Regex

object MermaidVisualization {

  private def convertToStandardMermaid(source: String): String = {
    // Step 1: Handle the @{ shape: ..., label: "..." } syntax
    val customNodePattern: Regex = 
      """(node\d+)(:::[\w-]+)?@\{\s*shape:\s*([\w-]+)\s*,\s*label:\s*"([^"]*)"\s*\}""".r

    def convertShape(shape: String, label: String): String = shape.toLowerCase match {
      case "circle"   => s"(($label))"
      case "stadium"  => s"([$label])"
      case "hexagon" | "hex" => s"{{$label}}"
      case "diamond"  => s"{$label}"
      case "fork"     => s"(( ))"
      case _          => s"[$label]"
    }

    var result = customNodePattern.replaceAllIn(source, m => {
      val nodeId = m.group(1)
      val cssClass = Option(m.group(2)).getOrElse("")
      val shape = m.group(3)
      val label = m.group(4)
      s"$nodeId$cssClass${convertShape(shape, label)}"
    })

    // Step 2: Handle FontAwesome icons
    result = result
      .replaceAll("""fa:fa-envelope\s+""", "📧 ")
      .replaceAll("""fa:fa-clock\s+""", "⏰ ")
      .replaceAll("""fa:fa-bolt\s+""", "⚡ ")
      .replaceAll("""fa:fa-wrench\s+""", "🔧 ")

    // Step 3: Format with proper line breaks
    result = result
      .replaceAll("""\s+node(\d+)""", "\n    node$1")
      .replaceAll("""\s+classDef\s""", "\n    classDef ")
      .replaceAll("""\s+class\s""", "\n    class ")
      .replaceAll("""-->\s*""", " --> ")

    // Step 4: Ensure proper flowchart header
    if (!result.trim.startsWith("flowchart") && !result.trim.startsWith("graph")) {
      result = "flowchart TD\n    " + result.trim
    }

    // Step 5: Clean up
    result.split('\n')
      .map(_.replaceAll("""\s+""", " ").trim)
      .filter(_.nonEmpty)
      .mkString("\n")
  }

  def view(mermaidDefinition: String): Html[Msg] = {
    val uniqueId = s"mermaid-${System.currentTimeMillis()}"
    val convertedSource = convertToStandardMermaid(mermaidDefinition)
    
    div(cls := "workflow-diagram-container")(
      h4(cls := "diagram-title")(
        span("📊"),
        span("Workflow Execution Diagram")
      ),
      
      // The Mermaid diagram container
      div(
        cls := "mermaid",
        id := uniqueId,
        style := "background: white; min-height: 400px; padding: 20px; border-radius: 8px;"
      )(
        text(convertedSource)
      ),
      
      // Debug section
      details()(
        summary()("🔍 Debug Info"),
        div(
          h5()("Converted Mermaid:"),
          pre(style := "background: #f0f0f0; padding: 10px; font-size: 11px; overflow-x: auto;")(
            code()(convertedSource)
          )
        )
      )
    )
  }
}