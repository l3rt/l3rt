package lert.elasticsearch.matcher

import java.util.{Collections, Date}
import javax.inject.Inject

import com.fasterxml.jackson.databind.ObjectMapper
import lert.core.processor.AlertMessage
import lert.core.state.StateProvider
import lert.elasticsearch.ElasticSearchProcessorUtils._
import lert.elasticsearch.Response
import lert.elasticsearch.restclient.RestClient
import org.apache.http.entity.ContentType
import org.apache.http.nio.entity.NStringEntity

class QueryStringMatcher @Inject()(implicit objectMapper: ObjectMapper, stateProvider: StateProvider) extends Matcher {
  override def supports(params: Map[String, _]): Boolean =
    params.contains("queryString")

  override def query(ruleName: String, client: RestClient, params: Map[String, _]): Seq[AlertMessage] = {
    val lastSeenTimestamp = stateProvider.getRuleStatus(ruleName).map(_.lastSeenTimestamp).getOrElse(new Date())

    val query = Map(
      "query" -> Map(
        "bool" -> Map(
          "must" -> Seq(
            Map(
              "query_string" -> Map(
                "query" -> params("queryString")
              )
            ),
            Map(
              "range" -> Map(
                getTimestampField(params) -> Map("gt" -> lastSeenTimestamp)
              )
            )
          )
        )
      )
    )

    val body = new NStringEntity(objectMapper.writeValueAsString(query), ContentType.APPLICATION_JSON)
    client
      .performRequest("GET", s"/${getIndexName(params)}/_search", Map(), body)
      .to[Response]
      .hits.hits
      .map(hit => AlertMessage(hit._source))
  }
}
