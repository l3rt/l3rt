package lert.core.rule.target

import lert.core.config.Config
import lert.core.rule.target.SlackTarget.SLACK_URL
import okhttp3._

class SlackTarget {
  def send(channel: String, message: String)(implicit config: Config) = {
    val client = new OkHttpClient()
    val formBody = new FormBody.Builder()
      .add("token", config.targetSettings.slack.accessToken)
      .add("channel", channel)
      .add("text", message)
      .build()
    val request = new Request.Builder().url(SLACK_URL).post(formBody).build()
    client.newCall(request).execute()
  }
}

object SlackTarget {
  val SLACK_URL = "https://slack.com/api/chat.postMessage"
}