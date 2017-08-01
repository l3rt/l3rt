package lert.core.rule.target

import javax.inject.Inject

import lert.core.config.ConfigProvider
import io.evanwong.oss.hipchat.v2.HipChatClient
import io.evanwong.oss.hipchat.v2.rooms.MessageColor

class HipChatTarget @Inject()(configProvider: ConfigProvider) {
  def send(room: String, message: String, color: String, notify: Boolean) = {
    val config = configProvider.config

    val client = new HipChatClient(config.targetSettings.hipchat.accessToken)

    val builder = client.prepareSendRoomNotificationRequestBuilder(room, message)
    builder.setColor(MessageColor.valueOf(color)).setNotify(notify).build.execute.get
  }
}
