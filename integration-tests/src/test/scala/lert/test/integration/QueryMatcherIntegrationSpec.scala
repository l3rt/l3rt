package lert.test.integration

import java.lang.Thread._
import java.nio.file.Files
import java.util.Date

import org.apache.http.HttpHost
import org.elasticsearch.client.RestClient

class QueryMatcherIntegrationSpec extends ElasticSearchIntegrationSpec {
  it should "log all message and not duplicate" in {
    implicit val client: RestClient = RestClient.builder(new HttpHost("localhost", container.mappedPort(9200), "http")).build()

    val tempFile = Files.createTempFile("rule", ".out")

    val now = new Date().getTime

    prepareApplication(createTempRule(
      s"""
         |rule {
         |    ruleName = "myTestRule"
         |    params = [
         |            "index": "logstash-*",
         |            "query": [
         |                    query: [
         |                        range: ["@timestamp": [gt: lastSeenTimestamp?: new Date($now)]]
         |                    ]
         |            ]
         |    ]
         |
           |    reaction { messages ->
         |        messages.each {
         |            file("${tempFile.toString}", "Message: " + it.data.toString())
         |        }
         |    }
         |}
      """.stripMargin))

    addMessage("test1")

    sleep(3000)

    assert(new String(Files.readAllBytes(tempFile)).lines.size == 1)

    addMessage("test2")
    addMessage("test3")

    sleep(3000)

    assert(new String(Files.readAllBytes(tempFile)).lines.size == 3)

    Files.delete(tempFile)
    stopApplication()
  }
}
