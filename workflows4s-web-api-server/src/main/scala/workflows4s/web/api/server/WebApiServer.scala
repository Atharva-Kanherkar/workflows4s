package workflows4s.web.api.server

import cats.effect.{IO, Resource}
import com.comcast.ip4s.*
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Server
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.http4s.Http4sServerInterpreter

object WebApiServer {
  
  def apply(endpoints: List[ServerEndpoint[Any, IO]]): WebApiServer = 
    new WebApiServer(endpoints)
}

class WebApiServer(endpoints: List[ServerEndpoint[Any, IO]]) {
  
  def start(host: String = "localhost", port: Int = 8081): Resource[IO, Server] = {
    val routes = Http4sServerInterpreter[IO]().toRoutes(endpoints)
    
    EmberServerBuilder
      .default[IO]
      .withHost(Host.fromString(host).getOrElse(host"localhost"))
      .withPort(Port.fromInt(port).getOrElse(port"8081"))
      .withHttpApp(routes.orNotFound)
      .build
  }
}