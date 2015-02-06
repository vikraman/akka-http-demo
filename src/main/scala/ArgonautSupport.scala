package akka.http.marshallers.argonaut

import argonaut._
import Argonaut._
import scala.language.implicitConversions
import scala.concurrent.ExecutionContext
import akka.stream.FlowMaterializer
import akka.http.marshalling.{ ToEntityMarshaller, Marshaller }
import akka.http.unmarshalling.{ FromEntityUnmarshaller, Unmarshaller }
import akka.http.model.{ ContentTypes, HttpCharsets }
import akka.http.model.MediaTypes.`application/json`

trait ArgonautSupport {

  implicit def argonautUnmarshallerConverter[T](reader: DecodeJson[T])(implicit ec: ExecutionContext, mat: FlowMaterializer): FromEntityUnmarshaller[T] =
    argonautUnmarshaller(reader, ec, mat)

  implicit def argonautUnmarshaller[T](implicit reader: DecodeJson[T], ec: ExecutionContext, mat: FlowMaterializer): FromEntityUnmarshaller[T] =
    argonautJsonUnmarshaller.map(reader.decodeJson(_).toOption.get)

  implicit def argonautJsonUnmarshaller(implicit ec: ExecutionContext, mat: FlowMaterializer): FromEntityUnmarshaller[Json] =
    Unmarshaller.byteStringUnmarshaller.forContentTypes(`application/json`).mapWithCharset { (data, charset) ⇒
      val input =
        if (charset == HttpCharsets.`UTF-8`) data.utf8String
        else data.decodeString(charset.nioCharset.name)
      //FIXME:
      Parse.parse(input).getOrElse(jNull)
    }

  implicit def argonautMarshallerConverter[T](e: EncodeJson[T])(ec: ExecutionContext): ToEntityMarshaller[T] =
    argonautMarshaller[T](e, ec)

  implicit def argonautMarshaller[T](implicit e: EncodeJson[T], ec: ExecutionContext): ToEntityMarshaller[T] =
    argonautJsonMarshaller[T].compose(e.encode)

  implicit def argonautJsonMarshaller[T](implicit e: EncodeJson[T], ec: ExecutionContext): ToEntityMarshaller[Json] =
    Marshaller.StringMarshaller.wrap(ContentTypes.`application/json`)(_.nospaces)
}

object ArgonautSupport extends ArgonautSupport
