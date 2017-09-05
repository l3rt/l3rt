package lert.core.rule

import java.util
import java.util.{Collections, Date}
import javax.inject.Inject

import scala.beans.BeanProperty
import scala.collection.JavaConverters._

import lert.core.ProcessorLoader
import lert.core.config.{Config, ConfigProvider}
import lert.core.rule.target.{EmailTarget, HipChatTarget, SlackTarget}
import lert.core.utils.JavaUtils
import com.typesafe.scalalogging.LazyLogging
import groovy.lang.Closure
import lert.core.state.{State, StateProvider}

class RuleDelegate @Inject()(hipChatTarget: HipChatTarget,
                             emailTarget: EmailTarget,
                             slackTarget: SlackTarget,
                             configProvider: ConfigProvider,
                             processorLoader: ProcessorLoader,
                             stateProvider: StateProvider) extends LazyLogging {
  @BeanProperty
  var sourceName: String = _

  @BeanProperty
  var ruleName: String = _

  @BeanProperty
  var params: java.util.Map[String, _] = _

  @BeanProperty
  val config: Config = configProvider.config

  private lazy val state: Option[State] = Option(stateProvider).flatMap(_.getRuleStatus(ruleName))

  var reactionWasCalled = false

  @BeanProperty
  var payload: java.util.Map[String, _] = _

  @BeanProperty
  var skip: Boolean = false

  @BeanProperty
  var memorize: java.util.Map[String, _] = new util.HashMap[String, Any]()

  def getLastExecutionTime: Date = state.map(_.lastExecutionTime).orNull

  def getLastProcessedId: util.Set[String] = state.map(_.lastProcessedIds.asJava).orNull

  def getLastSeenId: String = state.map(_.lastSeenId).orNull

  def getLastSeenTimestamp: Date = state.map(_.lastSeenTimestamp).orNull

  def getMemorizedData: util.Map[_, _] =
    state
      .map(_.customData)
      .map(JavaUtils.toJava)
      .map(_.asInstanceOf[util.Map[Any, Any]])
      .getOrElse(Collections.emptyMap[Any, Any]())

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

    if (!skip) {
      // Explicitly called to initialize the lazy value at the proper time
      state.foreach(_ => ())

      val config = configProvider.config
      val source = config.sources.find(_.name == sourceName)
        .orElse(config.sources.headOption)
        .getOrElse(throw new IllegalStateException("No sources defined"))
      val processor = processorLoader.load(source.sourceType)
      val preparedParams = Option(params).map(_.asScala.toMap).getOrElse(Map())

      val executionTime = new Date()
      val lastSeenData = processor.lastSeenData(ruleName, source, preparedParams)
      val messages = processor.loadMessages(ruleName, source, preparedParams)

      cl.call(
        messages
          .map(_.data)
          .map(JavaUtils.toJava)
          .map(_.asInstanceOf[util.Map[String, _]])
          .map(Message)
          .asJava
      )

      stateProvider.logRule(
        State(
          ruleName,
          messages.filter(_.data.contains("id")).map(_.data("id").toString).toSet, // TODO "id" -> ???
          executionTime,
          lastSeenData.map(_.lastSeenId).orNull,
          lastSeenData.map(_.lastSeenTimestamp).orNull,
          memorize.asScala.toMap
        )
      )
    }
  }
}

case class Message(data: util.Map[String, _])