package lert.test.integration

import java.lang.Thread.sleep
import java.nio.file.Files

import org.apache.http.HttpHost
import org.elasticsearch.client.RestClient

class QueryStringMatcherIntegrationSpec extends ElasticSearchIntegrationSpec {
  it should "log all message and not duplicate" in {
    implicit val client: RestClient = RestClient.builder(new HttpHost("localhost", container.mappedPort(9200), "http")).build()

    val tempFile = Files.createTempFile("rule", ".out")

    prepareApplication(createTempRule(
      s"""
         |rule {
         |    ruleName = "myTestRule"
         |    params = [
         |            "index": "logstash-*",
         |            queryString: "message:error",
         |            config: [
         |                    sources: [
         |                            ["url": "elasticSearch:http://localhost:${container.mappedPort(9200)}"]
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

    addMessage("test error 1")
    addMessage("test message 1")

    sleep(3000)

    assert(new String(Files.readAllBytes(tempFile)).lines.size == 1)

    addMessage("test error 2")
    addMessage("test message 2")
    addMessage("test error 3")
    addMessage("test message 3")

    sleep(3000)

    assert(new String(Files.readAllBytes(tempFile)).lines.size == 3)

    Files.delete(tempFile)
    stopApplication()
  }
}

