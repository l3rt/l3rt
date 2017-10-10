package lert.core.rule.target

import lert.core.config.Config
import io.evanwong.oss.hipchat.v2.HipChatClient
import io.evanwong.oss.hipchat.v2.rooms.MessageColor

class HipChatTarget {
  def send(room: String, message: String, color: String, notify: Boolean)(implicit config: Config) = {
    val client = new HipChatClient(config.targetSettings.hipchat.accessToken)

    val builder = client.prepareSendRoomNotificationRequestBuilder(room, message)
    builder.setColor(MessageColor.valueOf(color)).setNotify(notify).build.execute.get
  }
}
