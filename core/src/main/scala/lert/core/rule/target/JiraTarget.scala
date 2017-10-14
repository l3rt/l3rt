package lert.core.rule.target

import java.util.Base64

import lert.core.config.Config
import okhttp3._

class JiraTarget {
  def send(project: String, summary: String, description: String, issueType: String)(implicit config: Config): Unit = {
    val client = new OkHttpClient()
    val jsonType = MediaType.parse("application/json; charset=utf-8")
    val bodyStr =
      s"""
         |{
         |    "fields": {
         |       "project":
         |       {
         |          "key": "$project"
         |       },
         |       "summary": "$summary",
         |       "description": "$description",
         |       "issuetype": {
         |          "name": "$issueType"
         |       }
         |   }
         |}
      """.stripMargin
    val formBody = RequestBody.create(jsonType, bodyStr)
    val jiraSettings = config.targetSettings.jira
    val jiraUrl = jiraSettings.url
    val authString = new String(Base64.getEncoder.encode(s"${jiraSettings.username}:${jiraSettings.password}".getBytes))
    val request = new Request.Builder().url(s"$jiraUrl/rest/api/2/issue").header("Authorization", s"Basic $authString").post(formBody).build()
    val response = client.newCall(request).execute()
    if (response.code() > 299) {
      try {
        throw new RuntimeException(response.body().string())
      } finally {
        response.body().close()
      }
    }
  }
}


