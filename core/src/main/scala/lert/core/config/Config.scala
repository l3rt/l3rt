package lert.core.config

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
case class Config(delay: Int = 0,
                  sources: Seq[Source] = null,
                  rules: Seq[String] = null,
                  targetSettings: TargetSettings = null,
                  home: String = null)

case class Source(name: String, sourceType: String, params: Map[String, String])

case class TargetSettings(hipchat: HipchatSettings = null,
                          mailServer: MailServerSettings = null,
                          slack: SlackSettings = null)

case class HipchatSettings(accessToken: String, baseUrl: String)

case class SlackSettings(accessToken: String)

case class MailServerSettings(host: String,
                              port: String,
                              auth: Boolean,
                              username: String,
                              password: String)
