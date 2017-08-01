package lert.elasticsearch

import java.text.SimpleDateFormat
import java.util.{Collections, Date}
import javax.inject.Inject

import lert.core.cache.GlobalCache
import lert.core.config.Source
import lert.core.processor.{AlertMessage, Processor}
import lert.core.status.{Status, StatusProvider}
import lert.elasticsearch.ElasticSearchProcessor._
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.inject.Injector
import org.apache.http.HttpHost
import org.apache.http.entity.ContentType
import org.apache.http.nio.entity.NStringEntity
import org.elasticsearch.client.RestClient

class ElasticSearchProcessor @Inject()(objectMapper: ObjectMapper,
                                       statusProvider: StatusProvider,
                                       cache: GlobalCache) extends Processor {

  override def loadMessages(ruleName: String, source: Source, params: Map[String, Any]): Seq[AlertMessage] = {
    require(source.sourceType == classOf[ElasticSearchProcessor].getName)
    require(source.params.contains(PARAM_HOST))
    require(source.params.contains(PARAM_PORT))
    require(source.params.contains(PARAM_SCHEMA))

    val maybeStatus = statusProvider.getRuleStatus(ruleName)
    val lastProcessedTimestamp = maybeStatus.map(_.lastProcessedTimestamp).getOrElse(new Date(0))

    val index = params("index").toString
    val body = if (params.contains("query")) {
      val query = params("query").toString.replace("{lastProcessedTimestamp}", lastProcessedTimestamp.getTime.toString)
      new NStringEntity(query, ContentType.APPLICATION_JSON)
    } else {
      throw new IllegalArgumentException(s"Request details are not specified in the input parameters [$params]")
    }

    val (latestId, latestTimestamp) = getLastRecordIdAndDate(index, source)

    val entity = restClient(source).performRequest("GET", s"/$index/_search", Collections.emptyMap[String, String](), body).getEntity

    val messages = objectMapper.readValue(entity.getContent, classOf[Response])
      .hits.hits.map(hit => AlertMessage(hit._source))

    // TODO try to find latestId in messages if exists
    statusProvider.logRule(Status(ruleName, latestTimestamp, latestId))

    messages
  }

  protected def restClient(source: Source): RestClient = {
    cache.get(
      "elasticRestClient",
      source,
      RestClient.builder(new HttpHost(source.params(PARAM_HOST), source.params(PARAM_PORT).toInt, source.params(PARAM_SCHEMA))).build()
    )
  }

  protected def getLastRecordIdAndDate(index: String, source: Source): (String, Date) = {
    val entity = restClient(source).performRequest("GET", s"/$index/_search", Collections.emptyMap[String, String](), new NStringEntity(
      s"""
         |{
         |  "query": {
         |    "match_all": {}
         |  },
         |  "size": 1,
         |  "sort": [
         |    {
         |      "$DEFAULT_TIMESTAMP_FIELD": {
         |        "order": "desc"
         |      }
         |    }
         |  ]
         |}
      """.stripMargin, ContentType.APPLICATION_JSON)).getEntity

    val content = entity.getContent
    val record = try
      objectMapper.readValue(content, classOf[Response]).hits.hits.head
    finally
      content.close()

    val timestamp = record._source(source.params.getOrElse(PARAM_TIMESTAMP_FIELD, DEFAULT_TIMESTAMP_FIELD).toString).toString

    (record._id, DATE_FORMAT.parse(timestamp))
  }
}

object ElasticSearchProcessor {
  val DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")

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
