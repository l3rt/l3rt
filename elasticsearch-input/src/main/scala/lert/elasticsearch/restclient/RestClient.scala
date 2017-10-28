package lert.elasticsearch.restclient

import java.io.InputStream

import com.fasterxml.jackson.databind.ObjectMapper
import lert.core.config.Source
import org.apache.http.{Header, HttpEntity}

import scala.reflect.ClassTag

trait RestClient {
  def performRequest(method: String, endpoint: String, params: Map[String, String], entity: HttpEntity, headers: Header*): Response

  def supports(source: Source): Boolean
}

object RestClient {
  def apply(source: Source): RestClient = {
    Seq(
      new AWSRestClient(source),
      new ElasticSearchRestClient(source)
    ).find(_.supports(source))
      .getOrElse(throw new IllegalStateException(s"Couldn't find a suitable RestClient for $source"))
  }
}

case class Response(body: Array[Byte], status: Int) {
  def to[T: ClassTag](implicit objectMapper: ObjectMapper): T = {
    objectMapper.readValue(body, implicitly[reflect.ClassTag[T]].runtimeClass.asInstanceOf[Class[T]])
  }
}

object Response {
  def apply(body: InputStream, status: Int): Response = try {
    Response(Stream.continually(body.read).takeWhile(_ != -1).map(_.toByte).toArray, status)
  } finally {
    body.close()
  }
}