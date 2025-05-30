error id: file://<WORKSPACE>/workflows4s-example/src/main/scala/workflows4s/example/docs/courseregistration/CourseRegistrationWorkflow.scala:scala/Predef.String#
file://<WORKSPACE>/workflows4s-example/src/main/scala/workflows4s/example/docs/courseregistration/CourseRegistrationWorkflow.scala
empty definition using pc, found symbol in pc: scala/Predef.String#
empty definition using semanticdb
empty definition using fallback
non-local guesses:
	 -String#
	 -scala/Predef.String#
offset: 885
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
object CourseRegistrationWorkflow{
  // start_state
  sealed trait CourseRegistrationState
  object RegistrationState{
     case class Empty() extends CourseRegistrationState
     case class Browisng(studentId: String,
      semester : Int,
      currentCourseRequirement : String, // e.g. "CS101, CS102"
      avaulableCourses: List[CourseOption] //Let's say you have 5 choices avaiable for a mandatory course
      ) extends CourseRegistrationState
       case class SettingPriorities(
    studentId: String@@,
    semester: String,
    currentCourseRequirement: String, // Which CR they're currently prioritizing
    courseRequirements: Map[String, List[String]], // CR -> ordered priority list
    completedCRs: List[String] // Which CRs have priorities set
  ) extends CourseRegistrationState
  
  }

    
}
```


#### Short summary: 

empty definition using pc, found symbol in pc: scala/Predef.String#