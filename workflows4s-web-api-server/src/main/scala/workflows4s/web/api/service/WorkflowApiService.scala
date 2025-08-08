package workflows4s.web.api.service

import cats.effect.IO
import io.circe.Json
import io.circe.syntax.*
import workflows4s.web.api.model.*
import workflows4s.wio.model.{WIOMeta}
import workflows4s.wio.model.WIOExecutionProgressJson.given

trait WorkflowApiService {
  def listDefinitions(): IO[List[WorkflowDefinition]]
  def getDefinition(id: String): IO[WorkflowDefinition]
  def getDefinitionModel(id: String): IO[Json]
  def getInstance(definitionId: String, instanceId: String): IO[WorkflowInstance]
  def getProgress(definitionId: String, instanceId: String): IO[Json]
}

class MockWorkflowApiService extends WorkflowApiService {
  private val mockDefinitions = List(
    WorkflowDefinition("wf-1", "Workflow One"),
    WorkflowDefinition("wf-2", "Workflow Two"),
  )

  private val mockInstances = List(
    WorkflowInstance("inst-1", "wf-1", status = InstanceStatus.Running,   state = None),
    WorkflowInstance("inst-2", "wf-1", status = InstanceStatus.Completed, state = None),
    WorkflowInstance("inst-3", "wf-2", status = InstanceStatus.Failed,    state = None),
  )

  override def listDefinitions(): IO[List[WorkflowDefinition]] = IO.pure(mockDefinitions)

  override def getDefinition(id: String): IO[WorkflowDefinition] =
    IO.fromOption(mockDefinitions.find(_.id == id))(new Exception(s"Definition not found: $id"))

  override def getDefinitionModel(id: String): IO[Json] =
    IO.pure(
      Json.obj(
        "defId" -> Json.fromString(id),
        "model" -> Json.fromString("mock"),
      ),
    )

  override def getInstance(definitionId: String, instanceId: String): IO[WorkflowInstance] =
    for {
      _        <- getDefinition(definitionId)
      instance <- IO.fromOption(
        mockInstances.find(i => i.id == instanceId && i.definitionId == definitionId),
      )(new Exception(s"Instance not found: $instanceId"))
    } yield instance

  override def getProgress(definitionId: String, instanceId: String): IO[Json] = {
    import workflows4s.wio.model.WIOExecutionProgress
    for {
      inst <- getInstance(definitionId, instanceId)
      prog: WIOExecutionProgress[String] = inst.status match {
        case InstanceStatus.Running =>
          WIOExecutionProgress.Sequence(Seq(
            WIOExecutionProgress.Pure(WIOMeta.Pure(Some("Initialize"), None), Some(Right("initialized"))),
            WIOExecutionProgress.RunIO(WIOMeta.RunIO(Some("Processing"), None), None)
          ))
        case InstanceStatus.Completed =>
          WIOExecutionProgress.Sequence(Seq(
            WIOExecutionProgress.Pure(WIOMeta.Pure(Some("Initialize"), None), Some(Right("initialized"))),
            WIOExecutionProgress.RunIO(WIOMeta.RunIO(Some("Processing"), None), Some(Right("completed")))
          ))
        case InstanceStatus.Failed =>
          WIOExecutionProgress.Sequence(Seq(
            WIOExecutionProgress.Pure(WIOMeta.Pure(Some("Initialize"), None), Some(Right("initialized"))),
            WIOExecutionProgress.RunIO(WIOMeta.RunIO(Some("Processing"), None), Some(Left("error")))
          ))
      }
    } yield prog.asJson
  }
}