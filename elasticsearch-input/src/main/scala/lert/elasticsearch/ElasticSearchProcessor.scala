package lert.elasticsearch

import java.text.SimpleDateFormat
import java.util
import java.util.{Collections, Date, TimeZone}
import javax.inject.Inject

import scala.collection.JavaConverters._

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.ObjectMapper
import com.typesafe.scalalogging.LazyLogging
import lert.core.cache.GlobalCache
import lert.core.config.Source
import lert.core.processor.{AlertMessage, Processor}
import lert.core.status.{Status, StatusProvider}
import lert.elasticsearch.ElasticSearchProcessor._
import lert.elasticsearch.ElasticSearchProcessorUtils._
import lert.elasticsearch.matcher.Matcher
import org.apache.http.HttpHost
import org.elasticsearch.client.RestClient

class ElasticSearchProcessor @Inject()(implicit objectMapper: ObjectMapper,
                                       statusProvider: StatusProvider,
                                       cache: GlobalCache,
                                       matchers: util.Set[Matcher]) extends Processor with LazyLogging {

  override def loadMessages(ruleName: String, source: Source, params: Map[String, Any]): Seq[AlertMessage] = {
    require(source.sourceType == classOf[ElasticSearchProcessor].getName)
    require(source.params.contains(PARAM_HOST), s"$PARAM_HOST is not found in $source")
    require(source.params.contains(PARAM_PORT), s"$PARAM_PORT is not found in $source")
    require(source.params.contains(PARAM_SCHEMA), s"$PARAM_SCHEMA is not found in $source")

    val matcher = matchers.asScala.find(_.supports(params))
      .getOrElse(throw new IllegalArgumentException(s"Couldn't find a matcher for $params"))

    getLastRecordIdAndDate(getIndexName(params), source, params) match {
      case Some((latestId, latestTimestamp)) =>
        val messages = matcher.query(restClient(source), params, statusProvider.getRuleStatus(ruleName))

        // TODO try to find latestId in messages if exists
        statusProvider.logRule(Status(ruleName, latestTimestamp, latestId))

        messages
      case _ => Seq()
    }
  }

  protected def restClient(source: Source): RestClient = {
    cache.get(
      "elasticRestClient",
      source,
      RestClient.builder(new HttpHost(source.params(PARAM_HOST), source.params(PARAM_PORT).toInt, source.params(PARAM_SCHEMA))).build()
    )
  }

  protected def getLastRecordIdAndDate(index: String, source: Source, params: Map[String, _]): Option[(String, Date)] = {
    val record = restClient(source)
      .performRequest(
        "GET",
        s"/$index/_search",
        Collections.emptyMap[String, String](),
        httpEntity(Map(
          "query" -> Map("match_all" -> Map()),
          "size" -> 1,
          "sort" -> Seq(Map(getTimestampField(params) -> Map("order" -> "desc")))
        ))
      ).getEntity.to[Response].hits.hits.headOption

    record.map { v =>
      val timestamp = v._source(getTimestampField(params)).toString

      (v._id, DATE_FORMAT.parse(timestamp))
    }
  }
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
}

@JsonIgnoreProperties(ignoreUnknown = true)
case class Hit(_id: String, _source: Map[String, Any])

@JsonIgnoreProperties(ignoreUnknown = true)
case class Hits(hits: Seq[Hit])

@JsonIgnoreProperties(ignoreUnknown = true)
case class Response(hits: Hits)
