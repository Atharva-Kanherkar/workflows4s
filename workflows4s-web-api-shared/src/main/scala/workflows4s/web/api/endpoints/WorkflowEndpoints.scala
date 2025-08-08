package workflows4s.web.api.endpoints

import sttp.tapir.*
import sttp.tapir.generic.auto.*
import sttp.tapir.json.circe.*
import io.circe.Json
import workflows4s.web.api.model.*

object WorkflowEndpoints {

  private val baseEndpoint = endpoint.in("api" / "v1").errorOut(stringBody)

  val listDefinitions: PublicEndpoint[Unit, String, List[WorkflowDefinition], Any] =
    baseEndpoint.get.in("definitions").out(jsonBody[List[WorkflowDefinition]])

  val getDefinition: PublicEndpoint[String, String, WorkflowDefinition, Any] =
    baseEndpoint.get.in("definitions" / path[String]("defId")).out(jsonBody[WorkflowDefinition])

  // Keep as Json to avoid shared dependency on core model codecs
  val getDefinitionModel: PublicEndpoint[String, String, Json, Any] =
    baseEndpoint.get.in("definitions" / path[String]("defId") / "model").out(jsonBody[Json])

  val getInstance: PublicEndpoint[(String, String), String, WorkflowInstance, Any] =
    baseEndpoint.get
      .in("definitions" / path[String]("defId") / "instances" / path[String]("instanceId"))
      .out(jsonBody[WorkflowInstance])

  // New: instance progress (JSON)
  val getInstanceProgress: PublicEndpoint[(String, String), String, Json, Any] =
    baseEndpoint.get
      .in("definitions" / path[String]("defId") / "instances" / path[String]("instanceId") / "progress")
      .out(jsonBody[Json])

  val createTestInstanceEndpoint: PublicEndpoint[String, String, WorkflowInstance, Any] =
    baseEndpoint.post.in("definitions" / path[String]("workflowId") / "instances" / "test").out(jsonBody[WorkflowInstance])

  val allEndpoints =
    List(listDefinitions, getDefinition, getDefinitionModel, getInstance, getInstanceProgress, createTestInstanceEndpoint)
}