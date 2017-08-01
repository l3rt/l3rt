package lert.core.config

import javax.annotation.Nullable

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
case class Config(delay: Int = 0,
                  sources: Seq[Source] = null,
                  rules: Seq[String] = null,
                  targetSettings: TargetSettings = null)

case class Source(name: String, sourceType: String, params: Map[String, String])

case class TargetSettings(hipchat: HipchatSettings = null,
                          mailServer: MailServerSettings = null)

case class HipchatSettings(accessToken: String, baseUrl: String)

case class MailServerSettings(host: String,
                              port: String,
                              auth: Boolean,
                              username: String,
                              password: String)
