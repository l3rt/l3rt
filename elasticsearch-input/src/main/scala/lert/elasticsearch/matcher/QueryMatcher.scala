package lert.elasticsearch.matcher

import java.util.Collections
import javax.inject.Inject

import com.fasterxml.jackson.databind.ObjectMapper
import lert.core.processor.AlertMessage
import lert.elasticsearch.ElasticSearchProcessorUtils._
import lert.elasticsearch.{CustomRestClient, Response}
import org.apache.http.entity.ContentType
import org.apache.http.nio.entity.NStringEntity

class QueryMatcher @Inject()(implicit objectMapper: ObjectMapper) extends Matcher {
  override def supports(params: Map[String, _]): Boolean =
    params.contains("query")

  override def query(ruleName: String, client: CustomRestClient, params: Map[String, _]): Seq[AlertMessage] = {
    val query = params("query") match {
      case q: String => q
      case q: Any => objectMapper.writeValueAsString(q)
    }
    val body = new NStringEntity(query, ContentType.APPLICATION_JSON)
    client
      .performRequest("GET", s"/${getIndexName(params)}/_search", Collections.emptyMap[String, String](), body)
      .to[Response]
      .hits.hits
      .map(hit => AlertMessage(hit._source))
  }
}
