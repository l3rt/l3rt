package lert.elasticsearch.matcher

import java.util
import java.util.{Collections, Date}
import javax.inject.Inject

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.ObjectMapper
import com.typesafe.scalalogging.LazyLogging
import lert.core.processor.AlertMessage
import lert.elasticsearch.{CustomRestClient, ElasticSearchProcessor}
import lert.elasticsearch.ElasticSearchProcessorUtils._
import lert.elasticsearch.matcher.CountMatcher._

import scala.collection.JavaConverters._
import scala.concurrent.duration.Duration

class CountMatcher @Inject()(implicit objectMapper: ObjectMapper) extends Matcher with LazyLogging {
  override def supports(params: Map[String, _]): Boolean = {
    params.contains(MATCHER_PARAMETER) && params(MATCHER_PARAMETER).toString.toLowerCase == "count"
  }

  override def query(ruleName: String, client: CustomRestClient, params: Map[String, _]): Seq[AlertMessage] = {
    require(params.contains(TIMEFRAME_PARAMETER), s"$TIMEFRAME_PARAMETER is not defined in $params")
    require(params.contains(FILTER_PARAMETER), s"$FILTER_PARAMETER is not defined in $params")

    val timeFrame = Duration(params(TIMEFRAME_PARAMETER).toString)
    val numberOfTimeFrames = params.getOrElse(NUMBER_OF_TIMEFRAMES, 1).toString.toInt

    val filter: Map[String, _] = params(FILTER_PARAMETER) match {
      case v: util.Map[String, _] => v.asScala.toMap
      case v: Any => throw new IllegalArgumentException(s"$v is not supported as a filter. Please use a Groovy Map")
    }

    val query = createQuery(timeFrame, numberOfTimeFrames, filter, params)
    logger.debug(s"CountMatcher: $query")

    val response = client.performRequest(
      "GET",
      s"/${getIndexName(params)}/_search",
      Collections.emptyMap[String, String](),
      httpEntity(query)
    ).to[Response]

    Option(response.aggregations).map(_.range.buckets) match {

      case Some(buckets) =>
        buckets.map {
          case Bucket(from, to, count) =>
            val dateFrom = ElasticSearchProcessor.DATE_FORMAT.parse(from)
            val dateTo = ElasticSearchProcessor.DATE_FORMAT.parse(to)
            AlertMessage(Map(
              "count" -> count,
              "from" -> dateFrom,
              "to" -> dateTo
            ))
        }.sortBy(_.data("from").asInstanceOf[Date])

      case _ => Seq()
    }
  }

  protected[matcher] def createQuery(timeFrame: Duration, numberOfTimeFrames: Int, filter: Map[String, _], params: Map[String, _], currentTime: Long = new Date().getTime): Map[String, _] = {
    val ranges = (0 until numberOfTimeFrames)
      .map(t => (currentTime - (t + 1) * timeFrame.toMillis) -> (currentTime - t * timeFrame.toMillis))
      .map { case (from, to) =>
        Map("from" -> from, "to" -> to)
      }
    Map(
      "query" -> Map("bool" -> Map("must" -> filter)),
      "aggs" -> Map("range" -> Map("date_range" -> Map(
        "field" -> getTimestampField(params),
        "ranges" -> ranges
      ))),
      "size" -> 0
    )
  }
}

object CountMatcher {
  val TIMEFRAME_PARAMETER = "timeframe"
  val NUMBER_OF_TIMEFRAMES = "numberOfTimeframes"
  val FILTER_PARAMETER = "filter"

  @JsonIgnoreProperties(ignoreUnknown = true)
  case class Response(aggregations: Aggregations, took: Long)

  @JsonIgnoreProperties(ignoreUnknown = true)
  case class Aggregations(range: Range)

  @JsonIgnoreProperties(ignoreUnknown = true)
  case class Range(buckets: Seq[Bucket])

  @JsonIgnoreProperties(ignoreUnknown = true)
  case class Bucket(from_as_string: String, to_as_string: String, doc_count: Long)

}
