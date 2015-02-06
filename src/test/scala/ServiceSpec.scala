import akka.event.NoLogging
import akka.http.marshallers.argonaut.ArgonautSupport._
import akka.http.model.ContentTypes._
import akka.http.model.{ HttpResponse, HttpRequest }
import akka.http.model.StatusCodes._
import akka.http.testkit.ScalatestRouteTest
import akka.stream.scaladsl.Flow
import org.scalatest._

class ServiceSpec extends FlatSpec with Matchers with ScalatestRouteTest with Service {
  override def testConfigSource = "akka.loglevel = WARNING"
  override def config = testConfig
  override val logger = NoLogging

  "akka-http-demo" should "respond to any query" in {
    Get(s"/") ~> routes ~> check {
      status shouldBe OK
      contentType shouldBe `application/json`
      responseAs[List[Int]] shouldBe (1 to 42).toList
    }
  }
}
