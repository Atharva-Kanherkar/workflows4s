package workflows4s.wio.model

import io.circe.{Encoder, Json}
import io.circe.syntax.*
import workflows4s.wio.model.WIOExecutionProgress.*

object WIOExecutionProgressJson {

  // Fallback for arbitrary state types
  private given [S]: Encoder[S] = Encoder.encodeString.contramap(_.toString)

  private def encodeResult[S](r: ExecutionResult[S])(using encS: Encoder[S]): Json =
    r match {
      case None               => Json.obj("_status" -> Json.fromString("NotStarted"))
      case Some(Right(state)) => Json.obj("_status" -> Json.fromString("Completed"), "state" -> encS(state))
      case Some(Left(err))    => Json.obj("_status" -> Json.fromString("Failed"), "error" -> Json.fromString(Option(err).fold("null")(_.toString)))
    }

  // Encoder for the progress ADT (no dependency on WIOModel/Meta codecs)
  given [S](using encS: Encoder[S]): Encoder[WIOExecutionProgress[S]] = Encoder.instance {
    case x @ Sequence(steps) =>
      Json.obj(
        "_type"  -> Json.fromString("Sequence"),
        "result" -> encodeResult(x.result),
        "steps"  -> steps.asJson
      )

    case Dynamic(_) =>
      Json.obj(
        "_type"  -> Json.fromString("Dynamic"),
        "result" -> encodeResult(None)
      )

    case RunIO(_, result) =>
      Json.obj(
        "_type"  -> Json.fromString("RunIO"),
        "result" -> encodeResult(result)
      )

    case HandleSignal(_, result) =>
      Json.obj(
        "_type"  -> Json.fromString("HandleSignal"),
        "result" -> encodeResult(result)
      )

    case HandleError(base, handler, _, result) =>
      Json.obj(
        "_type"   -> Json.fromString("HandleError"),
        "result"  -> encodeResult(result),
        "base"    -> base.asJson,
        "handler" -> handler.asJson
      )

    case End(result) =>
      Json.obj(
        "_type"  -> Json.fromString("End"),
        "result" -> encodeResult(result)
      )

    case Pure(_, result) =>
      Json.obj(
        "_type"  -> Json.fromString("Pure"),
        "result" -> encodeResult(result)
      )

    case x @ Loop(_, _, _, history) =>
      Json.obj(
        "_type"   -> Json.fromString("Loop"),
        "result"  -> encodeResult(x.result),
        "history" -> history.asJson
      )

    case x @ Fork(branches, _, selected) =>
      Json.obj(
        "_type"    -> Json.fromString("Fork"),
        "result"   -> encodeResult(x.result),
        "branches" -> branches.asJson,
        "selected" -> selected.asJson
      )

    case Interruptible(base, trigger, handler, result) =>
      Json.obj(
        "_type"   -> Json.fromString("Interruptible"),
        "result"  -> encodeResult(result),
        "base"    -> base.asJson,
        "trigger" -> trigger.asJson
      ).deepMerge(handler.fold(Json.obj())(h => Json.obj("handler" -> h.asJson)))

    case Timer(_, result) =>
      Json.obj(
        "_type"  -> Json.fromString("Timer"),
        "result" -> encodeResult(result)
      )

    case Parallel(elements, result) =>
      Json.obj(
        "_type"    -> Json.fromString("Parallel"),
        "result"   -> encodeResult(result),
        "elements" -> elements.asJson
      )

    case Checkpoint(base, result) =>
      Json.obj(
        "_type"  -> Json.fromString("Checkpoint"),
        "result" -> encodeResult(result),
        "base"   -> base.asJson
      )

    case Recovery(result) =>
      Json.obj(
        "_type"  -> Json.fromString("Recovery"),
        "result" -> encodeResult(result)
      )
  }
}