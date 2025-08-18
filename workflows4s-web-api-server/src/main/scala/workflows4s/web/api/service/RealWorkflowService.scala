package workflows4s.web.api.service

import workflows4s.web.api.model.*
import cats.effect.IO
import io.circe.*
// REMOVED: import io.circe.syntax.*
import workflows4s.wio.model.WIOExecutionProgress
import workflows4s.wio.*
import workflows4s.runtime.WorkflowRuntime

class RealWorkflowService(workflowEntries: List[RealWorkflowService.WorkflowEntry[?]]) extends WorkflowApiService {

  def listDefinitions(): IO[List[WorkflowDefinition]] =
    IO.pure(workflowEntries.map(e => WorkflowDefinition(e.id, e.name)))

  def getDefinition(id: String): IO[WorkflowDefinition] =
    for {
      entry <- findEntry(id)
    } yield WorkflowDefinition(entry.id, entry.name)

  def getInstance(definitionId: String, instanceId: String): IO[WorkflowInstance] =
    for {
      entry    <- findEntry(definitionId)
      instance <- getRealInstance(entry, instanceId)
    } yield instance

  def getProgress(definitionId: String, instanceId: String): IO[Json] =
    for {
      entry    <- findEntry(definitionId)
      progress <- getRealProgress(entry, instanceId)
      // Converted to simple Json representation instead of complex ProgressResponse
      progressAsString = progress.map(state => Some(state.toString))
    } yield Json.obj(
      "workflowProgress" -> Json.fromString(progressAsString.toString),
      "instanceId" -> Json.fromString(instanceId),
      "definitionId" -> Json.fromString(definitionId)
    )

  private def findEntry(definitionId: String): IO[RealWorkflowService.WorkflowEntry[?]] =
    IO.fromOption(workflowEntries.find(_.id == definitionId))(new Exception(s"Definition not found: $definitionId"))

  private def progressToStatus(progress: WIOExecutionProgress[?]): InstanceStatus =
    progress.result match {
      case Some(Right(_)) => InstanceStatus.Completed
      case Some(Left(_))  => InstanceStatus.Failed
      case None           => InstanceStatus.Running
    }

  private def getRealInstance[Ctx <: WorkflowContext](
      entry: RealWorkflowService.WorkflowEntry[Ctx],
      instanceId: String,
  ): IO[WorkflowInstance] = {
    for {
      workflowInstance <- entry.runtime.createInstance(instanceId)
      currentState     <- workflowInstance.queryState()
      progress         <- workflowInstance.getProgress
    } yield WorkflowInstance(
      id = instanceId,
      definitionId = entry.id,
      status = progressToStatus(progress),
      state = Some(entry.stateEncoder(currentState)),
    )
  }

  private def getRealProgress[Ctx <: WorkflowContext](
      entry: RealWorkflowService.WorkflowEntry[Ctx],
      instanceId: String,
  ): IO[WIOExecutionProgress[WCState[Ctx]]] = {
    for {
      workflowInstance <- entry.runtime.createInstance(instanceId)
      progress         <- workflowInstance.getProgress
    } yield progress
  }
}

object RealWorkflowService {
  case class WorkflowEntry[Ctx <: WorkflowContext](
      id: String,
      name: String,
      runtime: WorkflowRuntime[IO, Ctx, String],
      stateEncoder: Encoder[WCState[Ctx]],
  )
}