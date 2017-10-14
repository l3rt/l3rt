package lert.core.rule.target

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
    val jiraUrl = config.targetSettings.jira.url
    val authString = config.targetSettings.jira.token
    val request = new Request.Builder().url(jiraUrl).header("Authorization", s"Basic ${authString}").post(formBody).build()
    client.newCall(request).execute()
  }
}


