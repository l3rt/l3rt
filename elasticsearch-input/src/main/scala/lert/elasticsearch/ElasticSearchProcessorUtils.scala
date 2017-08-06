package lert.elasticsearch

import scala.reflect.ClassTag

import com.fasterxml.jackson.databind.ObjectMapper
import lert.elasticsearch.ElasticSearchProcessor.{DEFAULT_TIMESTAMP_FIELD, PARAM_TIMESTAMP_FIELD}
import org.apache.http.HttpEntity
import org.apache.http.entity.ContentType
import org.apache.http.nio.entity.NStringEntity

object ElasticSearchProcessorUtils {
  def getTimestampField(params: Map[String, _]): String =
    params.getOrElse(PARAM_TIMESTAMP_FIELD, DEFAULT_TIMESTAMP_FIELD).toString

  def httpEntity(data: Map[String, _])(implicit objectMapper: ObjectMapper) =
    new NStringEntity(objectMapper.writeValueAsString(data), ContentType.APPLICATION_JSON)

  def getIndexName(params: Map[String, _]): String = params(ElasticSearchProcessor.PARAM_INDEX).toString

  implicit class HttpEntityMapper(val httpEntity: HttpEntity) extends AnyVal {
    def to[T: ClassTag](implicit objectMapper: ObjectMapper): T = {
      def ctag = implicitly[reflect.ClassTag[T]]
      val content = httpEntity.getContent
      try
        objectMapper.readValue(content, ctag.runtimeClass.asInstanceOf[Class[T]])
      finally
        content.close()
    }
  }

}
