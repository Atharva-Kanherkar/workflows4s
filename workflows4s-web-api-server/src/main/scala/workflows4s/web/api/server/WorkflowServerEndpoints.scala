package workflows4s.web.api.server

import cats.effect.IO
import cats.syntax.all.*
import workflows4s.web.api.endpoints.WorkflowEndpoints
import workflows4s.web.api.model.*
import workflows4s.web.api.service.WorkflowApiService
import sttp.tapir.server.ServerEndpoint

class WorkflowServerEndpoints(workflowService: WorkflowApiService) {

  private def createTestInstanceLogic(workflowId: String): Either[String, WorkflowInstance] = {
    val testInstanceId = s"test-${System.currentTimeMillis()}"
    Right(WorkflowInstance(testInstanceId, workflowId, InstanceStatus.Running, None))
  }

  val endpoints: List[ServerEndpoint[Any, IO]] = List(
    WorkflowEndpoints.listDefinitions.serverLogic(_ => {
      workflowService.listDefinitions().attempt.map(_.leftMap(_.getMessage))
    }),
    WorkflowEndpoints.getDefinition.serverLogic(id => {
      workflowService.getDefinition(id).attempt.map(_.leftMap(_.getMessage))
    }),
    WorkflowEndpoints.getInstance.serverLogic((defId, instanceId) => {
      workflowService.getInstance(defId, instanceId).attempt.map(_.leftMap(_.getMessage))
    }),
    WorkflowEndpoints.getInstanceProgress.serverLogic((defId, instanceId) => {
      workflowService.getProgress(defId, instanceId).attempt.map(_.leftMap(_.getMessage))
    }),
    WorkflowEndpoints.createTestInstanceEndpoint.serverLogic(workflowId => {
      IO.pure(createTestInstanceLogic(workflowId))
    }),
  )
}