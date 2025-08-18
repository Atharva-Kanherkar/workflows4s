package workflows4s.web.ui.models

import io.circe.Json

enum AppState {
  case Initializing
  case LoadingWorkflows
  case Ready(error: Option[String] = None)
  case LoadingInstance
  case LoadingProgress  
}

case class Model(
    appState: AppState,
    workflows: List[WorkflowDefinition],
    selectedWorkflowId: Option[String],
    instanceIdInput: String,
    instanceError: Option[String],
    currentInstance: Option[WorkflowInstance],
    showJsonState: Boolean,
    progressData: Option[Json],
    mermaidDiagram: Option[String],
    showMermaidDiagram: Boolean,
) {
  def loadingWorkflows: Model = copy(appState = AppState.LoadingWorkflows)
  def workflowsReady(workflows: List[WorkflowDefinition]): Model = 
    copy(appState = AppState.Ready(), workflows = workflows)
  def workflowsFailed(error: String): Model = 
    copy(appState = AppState.Ready(Some(error)))
  
  def withSelectedWorkflow(wfId: Option[String]): Model = 
    copy(selectedWorkflowId = wfId)
  def withInstanceIdInput(text: String): Model = 
    copy(instanceIdInput = text)
  
  def loadingInstance: Model = copy(appState = AppState.LoadingInstance, instanceError = None)
  def withInstance(instance: WorkflowInstance): Model = 
    copy(appState = AppState.Ready(), currentInstance = Some(instance), instanceError = None)
  def withInstanceError(error: String): Model = 
    copy(appState = AppState.Ready(), instanceError = Some(error), currentInstance = None)
  
  def toggleJsonState: Model = copy(showJsonState = !showJsonState)
  
 
  def loadingProgress: Model = copy(appState = AppState.LoadingProgress)
  def withProgress(progress: Json, mermaid: String): Model = 
    copy(
      appState = AppState.Ready(), 
      progressData = Some(progress), 
      mermaidDiagram = Some(mermaid)
    )
  def withProgressError(error: String): Model = 
    copy(appState = AppState.Ready(), instanceError = Some(error))
  def toggleMermaidDiagram: Model = copy(showMermaidDiagram = !showMermaidDiagram)
}

object Model {
  def initial: Model = Model(
    appState = AppState.Initializing,
    workflows = List.empty,
    selectedWorkflowId = None,
    instanceIdInput = "",
    instanceError = None,
    currentInstance = None,
    showJsonState = false,
    progressData = None,
    mermaidDiagram = None,
    showMermaidDiagram = false,
  )
}