package lert.core.rule.target

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.inject.Inject
import lert.core.config.Config
import lert.core.rule.target.JiraTarget._
import lert.core.utils.HttpUtils
import okhttp3._

class JiraTarget @Inject()(objectMapper: ObjectMapper) {
  lazy val client = new OkHttpClient()

  def send(project: String, summary: String, description: String, issueType: String)(implicit config: Config): Unit = {
    val settings = config.targetSettings.jira

    val formBody = RequestBody.create(JSON_MEDIA_TYPE, objectMapper.writeValueAsBytes(Issue(
      Fields(
        Project(project),
        summary,
        description,
        IssueType(issueType)
      )
    )))

    val baseUrl = settings.url + (if (settings.url.endsWith("/")) "" else "/") + "rest/api/2/issue"
    val request = new Request.Builder().url(baseUrl).header("Authorization", s"Basic ${HttpUtils.basicAuth(settings.username, settings.password)}").post(formBody).build()
    val response = client.newCall(request).execute()
    if (response.code() > 299) {
      try {
        throw new RuntimeException(s"Couldn't process Jira REST call due to: ${response.body().string()}")
      } finally {
        response.body().close()
      }
    }
  }
}

object JiraTarget {
  final val JSON_MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8")

  case class Issue(fields: Fields)

  case class Fields(project: Project, summary: String, description: String, issuetype: IssueType)

  case class Project(key: String)

  case class IssueType(name: String)

}
