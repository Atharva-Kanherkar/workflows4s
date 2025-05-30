error id: `<none>`.
file://<WORKSPACE>/workflows4s-example/src/main/scala/workflows4s/example/pekko/HttpRoutes.scala
empty definition using pc, found symbol in pc: `<none>`.
empty definition using semanticdb
empty definition using fallback
non-local guesses:
	 -org/apache/pekko/http/scaladsl/server/Directives.service.
	 -service.
	 -scala/Predef.service.
offset: 2038
uri: file://<WORKSPACE>/workflows4s-example/src/main/scala/workflows4s/example/pekko/HttpRoutes.scala
text:
```scala
package workflows4s.example.pekko

import cats.effect.unsafe.IORuntime
import cats.implicits.toContravariantOps
import com.github.pjfanning.pekkohttpcirce.FailFastCirceSupport
import io.circe.generic.semiauto.deriveEncoder
import io.circe.syntax.EncoderOps
import io.circe.{Encoder, Json}
import org.apache.pekko.actor.typed.ActorSystem
import org.apache.pekko.http.scaladsl.server.Directives.*
import org.apache.pekko.http.scaladsl.server.Route
import workflows4s.example.withdrawal.WithdrawalData
import workflows4s.example.withdrawal.WithdrawalService.{Fee, Iban}
import workflows4s.example.withdrawal.WithdrawalSignal.CreateWithdrawal
import workflows4s.example.withdrawal.checks.{CheckResult, ChecksInput, ChecksState}

class HttpRoutes(actorSystem: ActorSystem[?], service: WithdrawalWorkflowService)(using ioRuntime: IORuntime) extends FailFastCirceSupport {
  given checksInput: Encoder[ChecksInput]                             = Encoder.instance(_.checks.keys.map(_.value).asJson)
  given checksResult: Encoder[CheckResult]                            = Encoder.instance(_ => Json.Null)
  given checksResultFinished: Encoder[CheckResult.Finished]           = Encoder.instance(_ => Json.Null)
  given ChecksStateEncoder: Encoder[ChecksState]                      = deriveEncoder[ChecksState]
  given ChecksStateInprogressEncoder: Encoder[ChecksState.InProgress] = ChecksStateEncoder.narrow
  given ChecksStateDecicedEncoder: Encoder[ChecksState.Decided]       = ChecksStateEncoder.narrow
  given stateEncoder: Encoder.AsObject[WithdrawalData]                = deriveEncoder[WithdrawalData]

  val routes: Route = {
    path("withdrawals") {
      get {
        onSuccess(service.listWorkflows.unsafeToFuture()) { list =>
          complete(list)
        }
      }
    } ~
      path("withdrawals" / Segment) { id =>
        post {
          onSuccess(service.startWorkflow(id, CreateWithdrawal(id, 100, Iban("123"))).unsafeToFuture()) {
            complete("OK")
          }
        } ~
          get {
            onSuccess(servi@@ce.getState(id).unsafeToFuture()) { state =>
              complete(state)
            }
          }
      }
  }
}

```


#### Short summary: 

empty definition using pc, found symbol in pc: `<none>`.