package lert.core.rule.target

import javax.inject.Inject

import lert.core.config.ConfigProvider
import lert.core.rule.target.SlackTarget.SLACK_URL
import okhttp3._

class SlackTarget @Inject()(configProvider: ConfigProvider) {
  def send(room: String, message: String) = {
    val config = configProvider.config
    val client = new OkHttpClient()
    val formBody = new FormBody.Builder()
      .add("token", config.targetSettings.slack.accessToken)
      .add("channel", room)
      .add("text", message)
      .build()
    val request = new Request.Builder().url(SLACK_URL).post(formBody).build()
    client.newCall(request).execute()
  }
}

object SlackTarget {
  val SLACK_URL = "https://slack.com/api/chat.postMessage"
}