import akka.actor.ActorSystem
import akka.event.{ LoggingAdapter, Logging }
import akka.http.Http
import akka.http.client.RequestBuilding
import akka.http.marshallers.argonaut.ArgonautSupport._
import akka.http.marshalling.ToResponseMarshallable
import akka.http.model.{ HttpResponse, HttpRequest }
import akka.http.model.StatusCodes._
import akka.http.server.Directives._
import akka.http.unmarshalling.Unmarshal
import akka.stream.{ ActorFlowMaterializer, FlowMaterializer }
import akka.stream.scaladsl.{ Sink, Source }
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import java.io.IOException
import scala.concurrent.{ ExecutionContextExecutor, Future }
import scala.math._

trait Service {
  implicit val system: ActorSystem
  implicit def executor: ExecutionContextExecutor
  implicit val materializer: FlowMaterializer

  def config: Config
  val logger: LoggingAdapter

  val routes = {
    logRequestResult("akka-http-microservice") {
      complete {
        OK -> (1 to 42).toList
      }
    }
  }
}

object AkkaHttpMicroservice extends App with Service {
  override implicit val system = ActorSystem()
  override implicit val executor = system.dispatcher
  override implicit val materializer = ActorFlowMaterializer()

  override val config = ConfigFactory.load()
  override val logger = Logging(system, getClass)

  Http().bind(
    interface = config.getString("http.interface"),
    port = config.getInt("http.port")
  ).startHandlingWith(routes)
}
