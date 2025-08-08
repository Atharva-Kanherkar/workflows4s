package workflows4s.web.api.server

import cats.effect.IO
import sttp.tapir.server.ServerEndpoint
import workflows4s.web.api.endpoints.WorkflowEndpoints
import workflows4s.web.api.service.WorkflowApiService

class WorkflowServerEndpoints(workflowService: WorkflowApiService) {

  private def handle[T](io: IO[T]): IO[Either[String, T]] =
    io.attempt.map(_.left.map(_.getMessage))

  val endpoints: List[ServerEndpoint[Any, IO]] = List(
    WorkflowEndpoints.listDefinitions.serverLogic(_ => handle(workflowService.listDefinitions())),
    WorkflowEndpoints.getDefinition.serverLogic(defId => handle(workflowService.getDefinition(defId))),
    WorkflowEndpoints.getDefinitionModel.serverLogic(defId => handle(workflowService.getDefinitionModel(defId))),
    WorkflowEndpoints.getInstance.serverLogic { case (defId, instanceId) =>
      handle(workflowService.getInstance(defId, instanceId))
    },
    WorkflowEndpoints.getInstanceProgress.serverLogic { case (defId, instanceId) =>
      handle(workflowService.getProgress(defId, instanceId))
    },
    WorkflowEndpoints.createTestInstanceEndpoint.serverLogic(workflowId =>
      handle(workflowService.getInstance(workflowId, "test-instance-1")),
    ),
  )
}