package lert.core.config

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

import scala.annotation.meta.field

@JsonIgnoreProperties(ignoreUnknown = true)
case class Config(@(Description@field)("Delay (milliseconds) between rule starts")
                  delay: Int = 5000,
                  @(Description@field)("Location of the rule(s) (e.g. /tmp/rules or /tmp/myRule.groovy)")
                  rules: String = null,
                  @(Description@field)("Sources")
                  sources: Seq[Source] = null,
                  @(Description@field)("Target specific settings (shared with all rules)")
                  targetSettings: TargetSettings = null,
                  @(Description@field)("Location of the L3rt's home. Used for the state storing")
                  home: String = null)

case class Source(@(Description@field)("Should uniquely specified in case of multiple sources")
                  name: Option[String] = None,
                  @(Description@field)("Connection url (e.g. elasticSearch:http://localhost:9200)")
                  url: String,
                  params: Option[Map[String, String]] = None)

case class TargetSettings(@(Description@field)("HipChat settings")
                          hipchat: HipchatSettings = null,
                          @(Description@field)("Mail Server (SMTP) settings")
                          mailServer: MailServerSettings = null,
                          @(Description@field)("Slack settings")
                          slack: SlackSettings = null,
                          @(Description@field)("Jira settings")
                          jira: JiraSettings = null)

case class HipchatSettings(@(Description@field)("Access token https://developer.atlassian.com/hipchat/guide/hipchat-rest-api")
                           accessToken: String,
                           @(Description@field)("\"https://api.hipchat.com/v2/\" or your company server")
                           baseUrl: String)

case class SlackSettings(@(Description@field)("Token")
                         accessToken: String)

case class MailServerSettings(@(Description@field)("e.g. \"smtp.gmail.com\"")
                              host: String,
                              @(Description@field)("e.g. \"465\"")
                              port: String,
                              @(Description@field)("e.g. true")
                              auth: Boolean,
                              @(Description@field)("e.g. \"myemail@test.com\"")
                              username: String,
                              @(Description@field)("your password")
                              password: String)

case class JiraSettings(@(Description@field)("e.g. \"http://jira.company.com\"")
                        url: String,
                        @(Description@field)("Jira username")
                        username: String,
                        @(Description@field)("Jira password")
                        password: String)