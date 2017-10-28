package lert.elasticsearch

import java.util.{Date, UUID}

import lert.core.config.Source
import lert.elasticsearch.restclient.AWSRestClient
import org.apache.http.entity.ContentType
import org.apache.http.nio.entity.NStringEntity

object AWSDataGenerator extends App {
  val message = UUID.randomUUID().toString
  val record =
    s"""{
       |   "path" : "/testlog",
       |   "@timestamp" : "${ElasticSearchProcessor.DATE_FORMAT.format(new Date())}",
       |   "@version" : "1",
       |   "host" : "MacBook-Pro",
       |   "message" : "Message $message",
       |   "type" : "syslog"
       |}
      """.stripMargin

  new AWSRestClient(Source(

    url = s"elasticSearch:${System.getProperty("elasticsearch.url")}",
    params = Some(Map("awsRegion" -> System.getProperty("aws.region")))

  )).performRequest("PUT", s"/logstash-2017.09.05/syslog/$message", Map(), new NStringEntity(record, ContentType.APPLICATION_JSON))
}
