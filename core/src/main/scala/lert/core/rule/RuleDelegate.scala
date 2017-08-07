package lert.core.rule

import java.util
import javax.inject.Inject

import scala.beans.BeanProperty
import scala.collection.JavaConverters._
import lert.core.ProcessorLoader
import lert.core.config.ConfigProvider
import lert.core.rule.target.{EmailTarget, HipChatTarget, SlackTarget}
import lert.core.utils.JavaUtils
import com.typesafe.scalalogging.LazyLogging
import groovy.lang.Closure

class RuleDelegate @Inject()(hipChatTarget: HipChatTarget,
                             emailTarget: EmailTarget,
                             slackTarget: SlackTarget,
                             configProvider: ConfigProvider,
                             processorLoader: ProcessorLoader) extends LazyLogging {
  @BeanProperty
  var sourceName: String = _

  @BeanProperty
  var ruleName: String = _

  @BeanProperty
  var params: java.util.Map[String, _] = _

  @BeanProperty
  val config = configProvider.config

  var reactionWasCalled = false

  def log(message: Any): Unit = logger.info(message.toString)

  def debug(message: Any): Unit = logger.debug(message.toString)

  def error(message: Any): Unit = logger.error(message.toString)

  def hipchat(room: String, message: String, color: String, notify: Boolean): Unit =
    hipChatTarget.send(room, message, color, notify)

  def email(recipient: String, subject: String, body: String): Unit =
    emailTarget.send(recipient, subject, body)

  def slack(channel: String, message: String): Unit = slackTarget.send(channel, message)

  def reaction(cl: Closure[Unit]): Unit = {
    reactionWasCalled = true
    require(ruleName != null && ruleName.nonEmpty, "'ruleName' is required")
    logger.debug(s"Start rule's reaction with [sourceName: $sourceName]")

    val config = configProvider.config
    val source = config.sources.find(_.name == sourceName)
      .orElse(config.sources.headOption)
      .getOrElse(throw new IllegalStateException("No sources defined"))

    cl.call(
      processorLoader
        .load(source.sourceType)
        .loadMessages(ruleName, source, Option(params).map(_.asScala.toMap).getOrElse(Map()))
        .map(_.data)
        .map(JavaUtils.toJava)
        .map(_.asInstanceOf[util.Map[String, _]])
        .map(Message)
        .asJava
    )
  }
}

case class Message(data: util.Map[String, _])