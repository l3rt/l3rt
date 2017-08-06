package lert.elasticsearch.matcher

import java.util
import java.util.{Collections, Date}
import javax.inject.Inject

import scala.collection.JavaConverters._
import scala.concurrent.duration.Duration

import com.fasterxml.jackson.databind.ObjectMapper
import com.typesafe.scalalogging.LazyLogging
import lert.core.processor.AlertMessage
import lert.core.status.Status
import lert.elasticsearch.ElasticSearchProcessorUtils._
import lert.elasticsearch.Response
import lert.elasticsearch.matcher.CountMatcher._
import org.elasticsearch.client.RestClient

class CountMatcher @Inject()(implicit objectMapper: ObjectMapper) extends Matcher with LazyLogging {
  override def supports(params: Map[String, _]): Boolean = {
    params.contains(MATCHER_PARAMETER) && params(MATCHER_PARAMETER).toString.toLowerCase == "count"
  }

  override def query(client: RestClient, params: Map[String, _], status: Option[Status]): Seq[AlertMessage] = {
    require(params.contains(TIMEFRAME_PARAMETER), s"$TIMEFRAME_PARAMETER is not defined in $params")
    require(params.contains(FILTER_PARAMETER), s"$FILTER_PARAMETER is not defined in $params")

    val timeFrame = Duration(params(TIMEFRAME_PARAMETER).toString)
    val filter: Map[String, _] = params(FILTER_PARAMETER) match {
      case v: util.Map[String, _] => v.asScala.toMap
      case v: Any => throw new IllegalArgumentException(s"$v is not supported as a filter. Please use a Groovy Map")
    }

    val startTime = new Date().getTime - timeFrame.toMillis
    val query = Map("query" ->
      (filter.toSet ++
        Map("range" -> Map(getTimestampField(params) -> Map("gt" -> startTime))).toSet
        ).toMap
    )

    logger.debug(s"CountMatcher: $query")

    val result = client.performRequest(
      "GET",
      s"/${getIndexName(params)}/_count",
      Collections.emptyMap[String, String](),
      httpEntity(query)
    ).getEntity.to[Map[String, _]]

    Seq(AlertMessage(Map("count" -> result("count"))))
  }
}

object CountMatcher {
  val TIMEFRAME_PARAMETER = "timeframe"
  val FILTER_PARAMETER = "filter"
}
