package workflows4s.web.api.endpoints

import io.circe.Json
import sttp.tapir.*
import sttp.tapir.generic.auto.*
import sttp.tapir.json.circe.*
import workflows4s.web.api.model.*

object WorkflowEndpoints {
  private val baseEndpoint: PublicEndpoint[Unit, String, Unit, Any] =
    endpoint
      .in("api" / "v1")
      .errorOut(stringBody)

  // GET /api/v1/definitions
  val listDefinitions: PublicEndpoint[Unit, String, List[WorkflowDefinition], Any] =
    baseEndpoint
      .get
      .in("definitions")
      .out(jsonBody[List[WorkflowDefinition]])

  // GET /api/v1/definitions/{id}
  val getDefinition: PublicEndpoint[String, String, WorkflowDefinition, Any] =
    baseEndpoint
      .get
      .in("definitions" / path[String]("id"))
      .out(jsonBody[WorkflowDefinition])

  val createTestInstanceEndpoint: PublicEndpoint[String, String, WorkflowInstance, Any] =
    baseEndpoint
      .post
      .in("definitions" / path[String]("workflowId") / "instances" / "test")
      .out(jsonBody[WorkflowInstance])

  // GET /api/v1/definitions/{defId}/instances/{instanceId}
  val getInstance: PublicEndpoint[(String, String), String, WorkflowInstance, Any] =
    baseEndpoint
      .get
      .in("definitions" / path[String]("defId") / "instances" / path[String]("instanceId"))
      .out(jsonBody[WorkflowInstance])

  // Here we use Json instead of ProgressResponse to avoid schema derivation issues
  val getInstanceProgress: PublicEndpoint[(String, String), String, Json, Any] =
    baseEndpoint
      .get
      .in("definitions" / path[String]("defId") / "instances" / path[String]("instanceId") / "progress")
      .out(jsonBody[Json])
      .description("Get workflow instance progress")

  val allEndpoints = List(
    listDefinitions,
    getDefinition,
    getInstance,
    getInstanceProgress,
    createTestInstanceEndpoint
  )
}