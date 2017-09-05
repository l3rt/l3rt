package lert.elasticsearch.matcher

import java.io.ByteArrayInputStream
import java.util
import java.util.{Collections, Date}

import scala.concurrent.duration.Duration

import lert.core.BaseSpec
import lert.core.processor.AlertMessage
import org.apache.http.HttpEntity
import org.elasticsearch.client.{Response, RestClient}
import org.mockito.Matchers._
import org.mockito.Mockito.when
import org.scalatest.mockito.MockitoSugar._

class CountMatcherSpec extends BaseSpec {
  it should "create a valid query" in {
    val matcher = new CountMatcher()(null)
    val query = matcher.createQuery(Duration("1 s"), 2, Map(), Map(), 100000)
    assert(query("aggs") ==
      Map("range" -> Map("date_range" -> Map("field" -> "@timestamp",
        "ranges" -> Seq(
          Map("from" -> 99000, "to" -> 100000),
          Map("from" -> 98000, "to" -> 99000)
        )))))
  }

  it should "send a valid query to elastic" in {
    val matcher = new CountMatcher()(objectMapper)
    val restClient = mock[RestClient]
    val httpEntity = mock[HttpEntity]
    val response = mock[Response]

    when(restClient.performRequest(anyObject[String](),
      anyObject[String](),
      anyObject[util.Map[String, String]](),
      anyObject[HttpEntity]())).thenReturn(response)
    when(response.getEntity).thenReturn(httpEntity)
    when(httpEntity.getContent).thenReturn(new ByteArrayInputStream(
      """
        |{
        |  "took" : 5,
        |  "timed_out" : false,
        |  "_shards" : {
        |    "total" : 20,
        |    "successful" : 20,
        |    "failed" : 0
        |  },
        |  "hits" : {
        |    "total" : 39,
        |    "max_score" : 0.0,
        |    "hits" : [ ]
        |  },
        |  "aggregations" : {
        |    "range" : {
        |      "buckets" : [ {
        |        "key" : "2017-08-08T00:01:34.071Z-2017-08-08T00:04:34.071Z",
        |        "from" : 1.502150494071E12,
        |        "from_as_string" : "2017-08-08T00:01:34.071Z",
        |        "to" : 1.502150674071E12,
        |        "to_as_string" : "2017-08-08T00:04:34.071Z",
        |        "doc_count" : 0
        |      }, {
        |        "key" : "2017-08-08T00:04:34.071Z-2017-08-08T00:07:34.071Z",
        |        "from" : 1.502150674071E12,
        |        "from_as_string" : "2017-08-08T00:04:34.071Z",
        |        "to" : 1.502150854071E12,
        |        "to_as_string" : "2017-08-08T00:07:34.071Z",
        |        "doc_count" : 0
        |      } ]
        |    }
        |  }
        |}
      """.stripMargin.getBytes))

    val query = matcher.query(
      restClient,
      Map("timeframe" -> "3 min", "filter" -> Collections.emptyMap(), "index" -> "i")
    )
    assert(query == Seq(
      AlertMessage(
        Map(
          "count" -> 0,
          "from" -> new Date(1502150494071l),
          "to" -> new Date(1502150674071l)
        )
      ),
      AlertMessage(
        Map("count" -> 0,
          "from" -> new Date(1502150674071l),
          "to" -> new Date(1502150854071l)
        )
      ))
    )
  }
}
