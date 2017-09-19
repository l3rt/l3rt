package lert.test.integration

import java.lang.Thread.sleep
import java.nio.file.Files

import org.apache.http.HttpHost
import org.elasticsearch.client.RestClient

class CountMatcherIntegrationSpec extends ElasticSearchIntegrationSpec {
  it should "aggregate correctly by time frames" in {
    implicit val client: RestClient = RestClient.builder(new HttpHost("localhost", container.mappedPort(9200), "http")).build()

    val tempFile = Files.createTempFile("rule", ".out")

    addMessage("test1")
    addMessage("test1")

    prepareApplication(createTempRule(
      s"""
         |rule {
         |    ruleName = "countrule"
         |    params = [
         |            index: "logstash-*",
         |            matcher: "count",
         |            timeframe: "15s",
         |            numberOfTimeframes: 3,
         |            filter: [:]
         |    ]
         |
         |    skip = (new Date().getTime() - (lastExecutionTime?:new Date(0)).getTime() < 15000)
         |
         |    reaction { messages ->
         |        messages.each {
         |            file("${tempFile.toString}", it.data.count.toString())
         |        }
         |        file("${tempFile.toString}", "---")
         |    }
         |}
      """.stripMargin))

    sleep(15000)

    addMessage("test1")

    while (Files.readAllLines(tempFile).size() < 8) {
      Thread.sleep(1000)
    }

    assert(new String(Files.readAllBytes(tempFile)).trim ==
      """
        |0
        |0
        |2
        |---
        |0
        |2
        |1
        |---
      """.stripMargin.trim)

    Files.delete(tempFile)
    stopApplication()
  }
}
