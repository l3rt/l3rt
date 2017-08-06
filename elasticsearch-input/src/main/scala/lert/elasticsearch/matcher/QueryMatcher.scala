package lert.elasticsearch.matcher

import java.util.{Collections, Date}
import javax.inject.Inject

import com.fasterxml.jackson.databind.ObjectMapper
import lert.core.processor.AlertMessage
import lert.core.status.Status
import lert.elasticsearch.Response
import org.apache.http.entity.ContentType
import org.apache.http.nio.entity.NStringEntity
import org.elasticsearch.client.RestClient
import lert.elasticsearch.ElasticSearchProcessorUtils._

class QueryMatcher @Inject()(implicit objectMapper: ObjectMapper) extends Matcher {
  override def supports(params: Map[String, _]): Boolean =
    params.contains("query")

  override def query(client: RestClient, params: Map[String, _], status: Option[Status]): Seq[AlertMessage] = {
    val lastProcessedTimestamp = status.map(_.lastProcessedTimestamp).getOrElse(new Date(0))
    val query = params("query").toString.replace("{lastProcessedTimestamp}", lastProcessedTimestamp.getTime.toString)
    val body = new NStringEntity(query, ContentType.APPLICATION_JSON)
    client
      .performRequest("GET", s"/${getIndexName(params)}/_search", Collections.emptyMap[String, String](), body)
      .getEntity
      .to[Response]
      .hits.hits
      .map(hit => AlertMessage(hit._source))
  }
}
