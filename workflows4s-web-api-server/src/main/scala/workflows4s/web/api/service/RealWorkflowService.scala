package workflows4s.web.api.service

import cats.effect.IO
import io.circe.{Encoder, Json}
import io.circe.syntax.*
import workflows4s.runtime.WorkflowRuntime
import workflows4s.web.api.model.*
import workflows4s.wio.{WCState, WorkflowContext}
import workflows4s.wio.model.{WIOExecutionProgress}
import workflows4s.wio.model.WIOExecutionProgressJson.given

class RealWorkflowService(
    workflowEntries: List[RealWorkflowService.WorkflowEntry[?, ?]],
) extends WorkflowApiService {

  override def listDefinitions(): IO[List[WorkflowDefinition]] =
    IO.pure(workflowEntries.map(e => WorkflowDefinition(id = e.id, name = e.name)))

  override def getDefinition(id: String): IO[WorkflowDefinition] =
    findEntry(id).map(e => WorkflowDefinition(id = e.id, name = e.name))

  // Placeholder JSON model to keep compilation clean
  override def getDefinitionModel(id: String): IO[Json] =
    IO.pure(Json.obj("defId" -> Json.fromString(id), "model" -> Json.fromString("placeholder")))

  override def getInstance(definitionId: String, instanceId: String): IO[WorkflowInstance] =
    for {
      entry    <- findEntry(definitionId)
      instance <- getRealInstance(entry, instanceId)
    } yield instance

  override def getProgress(definitionId: String, instanceId: String): IO[Json] =
    for {
      entry   <- findEntry(definitionId)
      json    <- getRealInstanceProgressJson(entry, instanceId)
    } yield json

  // --- helpers ---
  private def findEntry(definitionId: String): IO[RealWorkflowService.WorkflowEntry[?, ?]] =
    IO.fromOption(workflowEntries.find(_.id == definitionId))(new Exception(s"Definition not found: $definitionId"))

  private def progressToStatus(progress: WIOExecutionProgress[?]): InstanceStatus =
    progress.result match {
      case Some(Right(_)) => InstanceStatus.Completed
      case Some(Left(_))  => InstanceStatus.Failed
      case None           => InstanceStatus.Running
    }

  private def getRealInstance[WorkflowId, Ctx <: WorkflowContext](
      entry: RealWorkflowService.WorkflowEntry[WorkflowId, Ctx],
      instanceId: String,
  ): IO[WorkflowInstance] = {
    val parsedId = entry.parseId(instanceId)
    for {
      workflowInstance <- entry.runtime.createInstance(parsedId)
      currentState     <- workflowInstance.queryState()
      progress         <- workflowInstance.getProgress
    } yield WorkflowInstance(
      id = instanceId,
      definitionId = entry.id,
      status = progressToStatus(progress),
      state = Some(entry.stateEncoder(currentState)),
    )
  }

  private def getRealInstanceProgressJson[WorkflowId, Ctx <: WorkflowContext](
      entry: RealWorkflowService.WorkflowEntry[WorkflowId, Ctx],
      instanceId: String,
  ): IO[Json] = {
    val parsedId = entry.parseId(instanceId)
    for {
      workflowInstance <- entry.runtime.createInstance(parsedId)
      progress         <- workflowInstance.getProgress
      json              = {
        given Encoder[WCState[Ctx]] = entry.stateEncoder
        progress.asJson
      }
    } yield json
  }
}

object RealWorkflowService {
  case class WorkflowEntry[WorkflowId, Ctx <: WorkflowContext](
      id: String,
      name: String,
      runtime: WorkflowRuntime[IO, Ctx, WorkflowId],
      parseId: String => WorkflowId,
      stateEncoder: Encoder[WCState[Ctx]],
  )
}