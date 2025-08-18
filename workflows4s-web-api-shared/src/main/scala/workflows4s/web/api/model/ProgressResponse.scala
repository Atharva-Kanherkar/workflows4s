package workflows4s.web.api.model

import io.circe.*
import io.circe.generic.semiauto.*
import sttp.tapir.Schema
import workflows4s.wio.model.WIOExecutionProgress
import workflows4s.wio.model.WIOExecutionProgressCodec.given

case class ProgressResponse(progress: WIOExecutionProgress[String])

object ProgressResponse {
  given Encoder[ProgressResponse] = deriveEncoder
  given Decoder[ProgressResponse] = deriveDecoder
  
  // Manual Tapir schema to avoid complex ADT derivation
  given Schema[ProgressResponse] = Schema.derived[ProgressResponse].description("Workflow execution progress response")
  
  given Schema[WIOExecutionProgress[String]] = Schema.string[WIOExecutionProgress[String]]
    .description("Workflow execution progress as JSON")
    .format("json")
}