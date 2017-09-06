package lert.test.integration

import java.nio.file.{Files, Path}
import java.util.{Collections, Date, UUID}

import com.dimafeng.testcontainers.{ForEachTestContainer, GenericContainer}
import lert.Application
import lert.core.BaseSpec
import lert.core.config.{Config, Source}
import lert.elasticsearch.ElasticSearchProcessor
import org.apache.http.HttpHost
import org.apache.http.entity.ContentType
import org.apache.http.nio.entity.NStringEntity
import org.elasticsearch.client.RestClient

class ElasticSearchIntegrationSpec extends BaseSpec with ForEachTestContainer {
  override val container = GenericContainer(
    "elasticsearch:2.4.4",
    command = Seq("elasticsearch", "--http.cors.enabled=true", "--script.inline=on", "--script.indexed=on", "--cluster.name=lert", "--http.cors.allow-origin=\"*\""),
    exposedPorts = Seq(9200)
  )

  it should "log all message and not duplicate" in {
    implicit val client: RestClient = RestClient.builder(new HttpHost("localhost", container.mappedPort(9200), "http")).build()

    addMessage("test1")

    val tempFile = Files.createTempFile("rule",".out")

    val rule = createTempRule(
      s"""
        |rule {
        |    ruleName = "myTestRule"
        |    params = [
        |            "index": "logstash-*",
        |            "query": [
        |                    query: [
        |                        range: ["@timestamp": [gt: lastSeenTimestamp]]
        |                    ]
        |            ]
        |    ]

        |    reaction { messages ->
        |        messages.each {
        |            file("${tempFile.toString}", "Message: " + it.data.toString())
        |        }
        |    }
        |}
      """.stripMargin)

    System.setProperty("config.body", objectMapper.writeValueAsString(Config(
      1000,
      sources = Seq(Source("test", "lert.elasticsearch.ElasticSearchProcessor", Map("host" -> "localhost", "port" -> container.mappedPort(9200).toString, "schema" -> "http"))),
      rules = Seq(rule.toString)
    )))
    Application.main(Array())

    Thread.sleep(3000)

    assert(new String(Files.readAllBytes(tempFile)).lines.size == 1)

    addMessage("test2")
    addMessage("test3")

    Thread.sleep(3000)

    assert(new String(Files.readAllBytes(tempFile)).lines.size == 3)

    Application.taskManager.stop()
  }

  private def createTempRule(rule: String): Path = {
    Files.write(Files.createTempFile("rule", UUID.randomUUID().toString), rule.getBytes)
  }

  private def addMessage(message: String)(implicit client: RestClient) = {
    val record =
      s"""{
         |   "path" : "/testlog",
         |   "@timestamp" : "${ElasticSearchProcessor.DATE_FORMAT.format(new Date())}",
         |   "@version" : "1",
         |   "host" : "MacBook-Pro",
         |   "message" : "$message",
         |   "type" : "syslog"
         |}
      """.stripMargin

    client.performRequest("PUT", s"/logstash-2017.09.05/syslog/${UUID.randomUUID().toString}", Collections.emptyMap[String, String](), new NStringEntity(record, ContentType.APPLICATION_JSON))
  }
}
