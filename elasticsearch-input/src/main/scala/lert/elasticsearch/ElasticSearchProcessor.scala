package lert.elasticsearch

import java.net.URI
import java.text.SimpleDateFormat
import java.util
import java.util.{Collections, TimeZone}
import javax.inject.Inject

import scala.collection.JavaConverters._
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.ObjectMapper
import com.typesafe.scalalogging.LazyLogging
import lert.core.cache.GlobalCache
import lert.core.config.Source
import lert.core.processor.{AlertMessage, LastSeenData, Processor}
import lert.elasticsearch.ElasticSearchProcessor._
import lert.elasticsearch.ElasticSearchProcessorUtils._
import lert.elasticsearch.matcher.Matcher
import org.apache.http.HttpHost
import org.elasticsearch.client.RestClient

class ElasticSearchProcessor @Inject()(implicit objectMapper: ObjectMapper,
                                       cache: GlobalCache,
                                       matchers: util.Set[Matcher]) extends Processor with LazyLogging {

  override def loadMessages(ruleName: String, source: Source, params: Map[String, Any]): Seq[AlertMessage] = {
    val matcher = matchers.asScala.find(_.supports(params))
      .getOrElse(throw new IllegalArgumentException(s"Couldn't find a matcher for $params"))

    matcher.query(restClient(source), params)
  }

  override def lastSeenData(ruleName: String, source: Source, params: Map[String, Any]): Option[LastSeenData] = {
    val record = restClient(source)
      .performRequest(
        "GET",
        s"/${getIndexName(params)}/_search",
        Collections.emptyMap[String, String](),
        httpEntity(Map(
          "query" -> Map("match_all" -> Map()),
          "size" -> 1,
          "sort" -> Seq(Map(getTimestampField(params) -> Map("order" -> "desc")))
        ))
      ).getEntity.to[Response].hits.hits.headOption

    record.map { v =>
      val timestamp = v._source(getTimestampField(params)).toString

      LastSeenData(DATE_FORMAT.parse(timestamp), v._id)
    }
  }

  protected def restClient(source: Source): RestClient = {
    val url = new URI(source.url.substring(SOURCE_URL_PREFIX.length))
    cache.get(
      "elasticRestClient",
      source,
      RestClient.builder(new HttpHost(url.getHost, url.getPort, url.getScheme)).build()
    )
  }

  def supports(source: Source): Boolean = source.url.startsWith(SOURCE_URL_PREFIX)
}

object ElasticSearchProcessor {
  val DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'") {
    setTimeZone(TimeZone.getTimeZone("UTC"))
  }

  val PARAM_HOST = "host"
  val PARAM_PORT = "port"
  val PARAM_SCHEMA = "schema"
  val PARAM_INDEX = "index"
  val PARAM_TIMESTAMP_FIELD = "timestampField"

  val DEFAULT_TIMESTAMP_FIELD = "@timestamp"

  val SOURCE_URL_PREFIX = "elasticSearch:"
}

@JsonIgnoreProperties(ignoreUnknown = true)
case class Hit(_id: String, _source: Map[String, Any])

@JsonIgnoreProperties(ignoreUnknown = true)
case class Hits(hits: Seq[Hit])

@JsonIgnoreProperties(ignoreUnknown = true)
case class Response(hits: Hits)
