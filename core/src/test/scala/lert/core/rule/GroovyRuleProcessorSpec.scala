package lert.core.rule

import java.io.ByteArrayInputStream
import java.util.{Collections, Date}

import scala.collection.mutable.ListBuffer

import lert.core.config.{Config, ConfigProvider, SimpleConfigProvider, Source}
import lert.core.processor.{AlertMessage, Processor}
import lert.core.rule.target.{EmailTarget, HipChatTarget, SlackTarget, TargetHelper}
import lert.core.{BaseSpec, ProcessorLoader}
import com.google.inject.Injector
import groovy.lang.Closure
import lert.core.rule.GroovyRuleProcessorSpec.RuleDelegateWithSink
import lert.core.status.{Status, StatusProvider}
import org.mockito.Matchers._
import org.mockito.Mockito.{spy, verify, when}
import org.scalatest.mockito.MockitoSugar._

class GroovyRuleProcessorSpec extends BaseSpec {
  it should "run the script" in {
    val processor = processorMock(Seq(AlertMessage(Map("test" -> Map("1" -> "2")))))
    val delegate = spy(new RuleDelegateWithSink(
      configProvider = SimpleConfigProvider(Config(sources = Seq(source))),
      processorLoader = processorLoaderMock(processor)
    ))
    runRule(
      """
        |rule {
        | sourceName = "test"
        | ruleName = "testrule"
        | params = ["test": "param"]
        | reaction {
        |   println(sourceName)
        | }
        |}
      """.stripMargin,
      delegate
    )

    assert(delegate.sourceName == "test")
    assert(delegate.ruleName == "testrule")
    verify(delegate).reaction(anyObject[Closure[Unit]]())
  }

  it should "trow an exception if rule has no reaction" in {
    val processor = processorMock(Seq())
    val delegate = spy(new RuleDelegateWithSink(
      configProvider = SimpleConfigProvider(Config(sources = Seq(source))),
      processorLoader = processorLoaderMock(processor)
    ))
    assertThrows[IllegalStateException] {
      runRule(
        """
          |rule {
          | println("test")
          |}
        """.
          stripMargin,
        delegate
      )
    }
  }

  it should "trow an exception if rule has no name" in {
    val processor = processorMock(Seq())
    val delegate = spy(new RuleDelegateWithSink(
      configProvider = SimpleConfigProvider(Config(sources = Seq(source))),
      processorLoader = processorLoaderMock(processor)
    ))
    assertThrows[IllegalArgumentException] {
      runRule(
        """
          |rule {
          | reaction {
          |   println("test")
          | }
          |}
        """.
          stripMargin,
        delegate
      )
    }
  }

  it should "send message via hipchat" in {
    val processor = processorMock(Seq())
    val hipChatTarget = mock[HipChatTarget]
    val delegate = spy(new RuleDelegateWithSink(
      hipChatTarget = hipChatTarget,
      configProvider = SimpleConfigProvider(Config(sources = Seq(source))),
      processorLoader = processorLoaderMock(processor)
    ))
    runRule(
      """
        |rule {
        | ruleName = "testrule"
        | reaction {
        |   hipchat("room", "message", "color", true)
        | }
        |}
      """.
        stripMargin,
      delegate
    )

    verify(hipChatTarget).send("room", "message", "color", true)
  }

  it should "send message via email" in {
    val processor = processorMock(Seq())
    val emailTarget = mock[EmailTarget]
    val delegate = spy(new RuleDelegateWithSink(
      emailTarget = emailTarget,
      configProvider = SimpleConfigProvider(Config(sources = Seq(source))),
      processorLoader = processorLoaderMock(processor)
    ))
    runRule(
      """
        |rule {
        | ruleName = "testrule"
        | reaction {
        |   email("recipient", "subj", "body")
        | }
        |}
      """.
        stripMargin,
      delegate
    )

    verify(emailTarget).send("recipient", "subj", "body")
  }

  it should "load last status" in {
    val processor = processorMock(Seq())
    val statusProvider = mock[StatusProvider]
    val delegate = spy(new RuleDelegateWithSink(
      statusProvider = statusProvider,
      configProvider = SimpleConfigProvider(Config(sources = Seq(source))),
      processorLoader = processorLoaderMock(processor)
    ))

    when(statusProvider.getRuleStatus("testrule")).thenReturn(Some(Status("testrule", Set("1"), new Date(1234), "2", new Date(4321))))

    runRule(
      """
        |rule {
        | ruleName = "testrule"
        | sink = lastExecutionTime
        | sink = lastProcessedId
        | sink = lastSeenId
        | sink = lastSeenTimestamp
        | reaction {
        |
        | }
        |}
      """.
        stripMargin,
      delegate
    )

    assert(delegate.sink == Seq(new Date(4321), Collections.singleton("1"), "2", new Date(1234)))
  }

  it should "process the skip variable" in {
    val processor = processorMock(Seq())
    val delegate = spy(new RuleDelegateWithSink(
      configProvider = SimpleConfigProvider(Config(sources = Seq(source))),
      processorLoader = processorLoaderMock(processor)
    ))
    runRule(
      """
        |rule {
        | ruleName = "testrule"
        | skip = true
        | reaction {
        |   sink = true
        | }
        |}
      """.
        stripMargin,
      delegate
    )

    assert(delegate.sink == Seq())
  }

  private def source = Source("test", "testSource", Map())

  private def processorMock(messages: Seq[AlertMessage]): Processor = {
    val processor = mock[Processor]
    when(processor.loadMessages(anyString(), anyObject[Source](), anyObject[Map[String, _]]())).thenReturn(messages)
    processor
  }

  private def processorLoaderMock(processor: Processor): ProcessorLoader = new ProcessorLoader(null) {
    override def load(sourceType: String): Processor = processor
  }

  private def runRule(rule: String, delegate: RuleDelegate) = {
    val injector = mock[Injector]
    TargetHelper.setInjector(injector)
    when(injector.getInstance(classOf[RuleDelegate])).thenReturn(delegate)

    new GroovyRuleRunner().process(new ByteArrayInputStream(
      rule.getBytes()
    ))
  }
}

object GroovyRuleProcessorSpec {

  class RuleDelegateWithSink(hipChatTarget: HipChatTarget = null,
                             emailTarget: EmailTarget = null,
                             slackTarget: SlackTarget = null,
                             configProvider: ConfigProvider = null,
                             processorLoader: ProcessorLoader = null,
                             statusProvider: StatusProvider = null) extends RuleDelegate(
    hipChatTarget,
    emailTarget,
    slackTarget,
    configProvider,
    processorLoader,
    statusProvider
  ) {

    val sink: ListBuffer[Any] = ListBuffer[Any]()

    def setSink(value: Any): Unit = sink += value
  }

}