package workflows4s.web.ui

import cats.effect.IO
import io.circe.Json
import sttp.client4.*
import sttp.client4.circe.*
import sttp.client4.impl.cats.FetchCatsBackend
import tyrian.Cmd
import workflows4s.web.ui.models.*

object Http {
  private val backend = FetchCatsBackend[IO]()
  private val baseUrl = "http://localhost:8081/api/v1"

  def loadWorkflows: Cmd[IO, Msg] = {
    val request = basicRequest
      .get(uri"$baseUrl/definitions")
      .response(asJson[List[WorkflowDefinition]])

    Cmd.Run(
      request
        .send(backend)
        .map(_.body)
        .map {
          case Right(workflows) => Msg.WorkflowsLoadedSuccess(workflows)
          case Left(error)      => Msg.WorkflowsLoadedFailure(s"Failed to decode: $error")
        }
        .handleError(err => Msg.WorkflowsLoadedFailure(err.getMessage)),
    )
  }

  def loadInstance(definitionId: String, instanceId: String): Cmd[IO, Msg] = {
    val request = basicRequest
      .get(uri"$baseUrl/definitions/$definitionId/instances/$instanceId")
      .response(asJson[WorkflowInstance])

    Cmd.Run(
      request
        .send(backend)
        .map(_.body)
        .map {
          case Right(instance) => Msg.InstanceLoadedSuccess(instance)
          case Left(error)     => Msg.InstanceLoadedFailure(s"Failed to decode: $error")
        }
        .handleError(err => Msg.InstanceLoadedFailure(err.getMessage)),
    )
  }

  def loadProgress(definitionId: String, instanceId: String): Cmd[IO, Msg] = {
    val progressRequest = basicRequest
      .get(uri"$baseUrl/definitions/$definitionId/instances/$instanceId/progress")
      .response(asJson[Json]) // Changed to Json

    Cmd.Run(
      progressRequest
        .send(backend)
        .map(_.body)
        .map {
          case Right(progressJson) => 
            // Generate Mermaid on the UI side as per Voytek's feedback
            val simpleMermaid = "flowchart TD\n  A[Workflow Progress] --> B[Loaded Successfully]"
            Msg.ProgressLoadedSuccess(progressJson, simpleMermaid) // Use progressJson directly
          case Left(error) => Msg.ProgressLoadedFailure(s"Failed to decode: $error")
        }
        .handleError(err => Msg.ProgressLoadedFailure(err.getMessage)),
    )
  }
}