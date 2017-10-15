package lert.core.utils

import java.util.Base64

object HttpUtils {
  def basicAuth(username: String, password: String) =
    new String(Base64.getEncoder.encode(s"$username:$password".getBytes))
}
