package lert.core.config

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
case class Config(delay: Int = 5000,
                  rules: String = null,
                  sources: Seq[Source] = null,
                  targetSettings: TargetSettings = null,
                  home: String = null)

case class Source(name: Option[String] = None, url: String, params: Option[Map[String, String]] = None)

case class TargetSettings(hipchat: HipchatSettings = null,
                          mailServer: MailServerSettings = null,
                          slack: SlackSettings = null,
                          jira: JiraSettings = null)

case class HipchatSettings(accessToken: String, baseUrl: String)

case class SlackSettings(accessToken: String)

case class MailServerSettings(host: String,
                              port: String,
                              auth: Boolean,
                              username: String,
                              password: String)

case class JiraSettings(url: String, token: String)
