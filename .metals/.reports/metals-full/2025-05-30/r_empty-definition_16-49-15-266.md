error id: file://<WORKSPACE>/workflows4s-example/src/main/scala/workflows4s/example/docs/courseregistration/CourseRegistrationWorkflow.scala:
file://<WORKSPACE>/workflows4s-example/src/main/scala/workflows4s/example/docs/courseregistration/CourseRegistrationWorkflow.scala
empty definition using pc, found symbol in pc: 
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 4571
uri: file://<WORKSPACE>/workflows4s-example/src/main/scala/workflows4s/example/docs/courseregistration/CourseRegistrationWorkflow.scala
text:
```scala
 package workflows4s.example.docs.courseregistration

import java.io.File
import cats.effect.IO
import org.camunda.bpm.model.bpmn.Bpmn
import workflows4s.bpmn.BpmnRenderer
import workflows4s.runtime.{InMemorySyncRuntime, InMemorySyncWorkflowInstance}
import workflows4s.wio.{SignalDef, WorkflowContext}
import scala.annotation.nowarn

@nowarn("msg=unused explicit parameter")
object CourseRegistrationWorkflow {
  
  // start_state
  sealed trait CourseRegistrationState
  object RegistrationState {
    case object Empty extends CourseRegistrationState
    
    case class Browsing(
      studentId: String,
      semester: String, // e.g. "2024-1"
      currentCourseRequirement: String, // e.g. "CR1", "CR2", "CR3", "CR4"
      availableCourses: List[CourseOption] // 5 choices available for current CR
    ) extends CourseRegistrationState
    
    case class SettingPriorities(
      studentId: String,
      semester: String,
      currentCourseRequirement: String, // Which CR they're currently prioritizing
      courseRequirements: Map[String, List[String]], // CR -> ordered priority list
      completedCRs: List[String] // Which CRs have priorities set
    ) extends CourseRegistrationState
    
    case class AllPrioritiesSet(
      studentId: String,
      semester: String,
      finalPriorities: Map[String, List[String]], // CR1 -> [choice1, choice2, choice3, choice4, choice5]
      readyForSubmission: Boolean
    ) extends CourseRegistrationState
    
    case class AwaitingAllotment(
      studentId: String,
      semester: String,
      submittedPriorities: Map[String, List[String]]
    ) extends CourseRegistrationState
    
    case class AllotmentComplete(
      studentId: String,
      semester: String,
      assignedCourses: Map[String, String], // CR1 -> "CS101-Prof.Smith"
      unassignedCRs: List[String], // CRs where no choice was available
      waitlistedCourses: Map[String, (String, Int)] // CR -> (course, position)
    ) extends CourseRegistrationState
    
    case class RegistrationFailed(
      state: CourseRegistrationState,
      reason: RegistrationError,
      failureTimestamp: String
    ) extends CourseRegistrationState
  }
  // end_state

  // Course option definition
  case class CourseOption(
    courseId: String,
    courseName: String,
    professor: String,
    timeslot: String,
    credits: Int,
    capacity: Int,
    enrolled: Int
  )

  // start_signals
  object Signals {
    val startBrowsing: SignalDef[BrowsingRequest, Unit] = SignalDef()
    val setPriorities: SignalDef[PrioritySelection, Unit] = SignalDef()
    val moveToNextCR: SignalDef[NextCourseRequirement, Unit] = SignalDef()
    val submitAllPriorities: SignalDef[FinalSubmission, Unit] = SignalDef()
    val confirmAllotment: SignalDef[AllotmentConfirmation, Unit] = SignalDef()
    
    case class BrowsingRequest(studentId: String, semester: String)
    case class PrioritySelection(
      courseRequirement: String, // "CR1"
      orderedChoices: List[String] // [choice1, choice2, choice3, choice4, choice5]
    )
    case class NextCourseRequirement(nextCR: String)
    case class FinalSubmission(confirmSubmission: Boolean)
    case class AllotmentConfirmation(acceptAllotment: Boolean)
  }
  // end_signals

  // start_events
  sealed trait RegistrationEvent
  object RegistrationEvent {
    case class BrowsingStarted(studentId: String, semester: String, courseRequirement: String) extends RegistrationEvent
    case class PrioritiesSet(courseRequirement: String, priorities: List[String]) extends RegistrationEvent
    case class MovedToNextCR(fromCR: String, toCR: String) extends RegistrationEvent
    case class AllPrioritiesSubmitted(finalPriorities: Map[String, List[String]]) extends RegistrationEvent
    case class AllotmentProcessed(assignments: Map[String, String], waitlists: Map[String, (String, Int)]) extends RegistrationEvent
    case class RegistrationCompleted(finalCourses: Map[String, String]) extends RegistrationEvent
  }
  // end_events

  // start_error
  sealed trait RegistrationError
  object RegistrationError {
    case class IncompletePriorities(missingCRs: List[String]) extends RegistrationError
    case class AllCoursesFull(courseRequirement: String) extends RegistrationError
    case class SystemError(message: String) extends RegistrationError
    case object RegistrationClosed extends RegistrationError
  }
  // end_error

  // start_context
  object Context extends WorkflowContext {
    override type Event = RegistrationEvent
    override type State = CourseRegistrationState
  }
  im@@port Context.*
  // end_context

  // start_draft_workflow
  // Simple draft workflow to visualize state transitions
  val draftWorkflow: WIO.Initial = 
    WIO.pure.value(RegistrationState.Empty) >>>
    WIO.pure.value(RegistrationState.Browsing("student-123", "2024-1", "CR1", List.empty)) >>>
    WIO.pure.value(RegistrationState.SettingPriorities("student-123", "2024-1", "CR1", Map.empty, List.empty)) >>>
    WIO.pure.value(RegistrationState.AllPrioritiesSet("student-123", "2024-1", Map.empty, true)) >>>
    WIO.pure.value(RegistrationState.AwaitingAllotment("student-123", "2024-1", Map.empty)) >>>
    WIO.pure.value(RegistrationState.AllotmentComplete("student-123", "2024-1", Map.empty, List.empty, Map.empty))
  // end_draft_workflow

  // Test function for the draft
  def runDraft(): Unit = {
    println("Course Registration Draft Workflow Created!")
    println("States defined:")
    println("- Empty")
    println("- Browsing")
    println("- SettingPriorities") 
    println("- AllPrioritiesSet")
    println("- AwaitingAllotment")
    println("- AllotmentComplete")
    
    // Generate draft diagram
    val bpmnModel = BpmnRenderer.renderWorkflow(draftWorkflow.toProgress.toModel, "course-registration-draft")
    Bpmn.writeModelToFile(new File("course-registration-draft.bpmn").getAbsoluteFile, bpmnModel)
    println("Draft BPMN file generated: course-registration-draft.bpmn")
  }
}
```


#### Short summary: 

empty definition using pc, found symbol in pc: 