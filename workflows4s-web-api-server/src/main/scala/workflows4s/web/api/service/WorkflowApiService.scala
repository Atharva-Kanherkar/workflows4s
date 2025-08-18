package workflows4s.web.api.service

import workflows4s.web.api.model.*
import cats.effect.IO
import io.circe.Json
import workflows4s.wio.model.{WIOExecutionProgress, WIOMeta}

trait WorkflowApiService {
  def listDefinitions(): IO[List[WorkflowDefinition]]
  def getDefinition(id: String): IO[WorkflowDefinition]
  def getInstance(definitionId: String, instanceId: String): IO[WorkflowInstance]
  def getProgress(definitionId: String, instanceId: String): IO[Json]
}

class MockWorkflowApiService extends WorkflowApiService {

  private val mockDefinitions = List(
    WorkflowDefinition("course-registration-v1", "Course Registration"),
    WorkflowDefinition("pull-request-v1",        "Pull Request"),
  )

  private val mockInstances = List(
    WorkflowInstance("instance-1", "course-registration-v1", InstanceStatus.Running, None),
    WorkflowInstance("instance-2", "course-registration-v1", InstanceStatus.Completed, None),
    WorkflowInstance("instance-3", "pull-request-v1", InstanceStatus.Failed, None),
  )

  def listDefinitions(): IO[List[WorkflowDefinition]] = IO.pure(mockDefinitions)

  def getDefinition(id: String): IO[WorkflowDefinition] =
    IO.fromOption(mockDefinitions.find(_.id == id))(new Exception(s"Definition not found: $id"))

  def getInstance(definitionId: String, instanceId: String): IO[WorkflowInstance] =
    for {
      _        <- IO.fromOption(mockDefinitions.find(_.id == definitionId))(new Exception(s"Definition not found: $definitionId"))
      instance <- IO.fromOption(mockInstances.find(i => i.id == instanceId && i.definitionId == definitionId))(
                    new Exception(s"Instance not found: $instanceId")
                  )
    } yield instance

  def getProgress(definitionId: String, instanceId: String): IO[Json] =
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
            WIOExecutionProgress.RunIO(WIOMeta.RunIO(Some("Processing"), None), Some(Left(())))
          ))
      }
    
    } yield Json.obj(
      "workflowProgress" -> Json.fromString(prog.toString),
      "status" -> Json.fromString(inst.status.toString)
    )
}