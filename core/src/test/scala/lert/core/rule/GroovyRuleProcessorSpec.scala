package lert.core.rule

import java.util.{Collections, Date}

import com.google.inject.Injector
import com.typesafe.config.{Config => TypesafeConfig}
import groovy.lang.Closure
import lert.core.config._
import lert.core.processor.{AlertMessage, LastSeenData, Processor}
import lert.core.rule.GroovyRuleProcessorSpec.RuleDelegateWithSink
import lert.core.rule.target.{EmailTarget, HipChatTarget, SlackTarget}
import lert.core.state.{RuleState, State, StateProvider}
import lert.core.{BaseSpec, ProcessorLoader}
import org.mockito.Matchers._
import org.mockito.Mockito.{spy, verify, when}
import org.mockito.{ArgumentCaptor, Mockito}
import org.scalatest.mockito.MockitoSugar._

import scala.collection.mutable.ListBuffer

class GroovyRuleProcessorSpec extends BaseSpec {
  it should "run the script" in {
    val processor = processorMock(Seq(AlertMessage(Map("test" -> Map("1" -> "2"), "id" -> "1"))))
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
      delegate,
      stateProviderMock("testrule")
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
        delegate,
        stateProviderMock("testrule")
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
        delegate,
        stateProviderMock("testrule")
      )
    }
  }

  it should "send message via hipchat" in {
    val processor = processorMock(Seq())
    val hipChatTarget = mock[HipChatTarget]
    val configProvider = SimpleConfigProvider(Config(sources = Seq(source)))
    implicit val config = configProvider.conf
    val delegate = spy(new RuleDelegateWithSink(
      hipChatTarget = hipChatTarget,
      configProvider = configProvider,
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
      delegate,
      stateProviderMock("testrule")
    )

    verify(hipChatTarget).send("room", "message", "color", true)
  }

  it should "send message via email" in {
    val processor = processorMock(Seq())
    val emailTarget = mock[EmailTarget]
    val configProvider = SimpleConfigProvider(Config(sources = Seq(source)))
    implicit val config = configProvider.conf
    val delegate = spy(new RuleDelegateWithSink(
      emailTarget = emailTarget,
      configProvider = configProvider,
      processorLoader = processorLoaderMock(processor),
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
      delegate,
      stateProviderMock("testrule")
    )

    verify(emailTarget).send("recipient", "subj", "body")
  }

  it should "load last status" in {
    val processor = processorMock(Seq())

    val delegate = spy(new RuleDelegateWithSink(
      configProvider = SimpleConfigProvider(Config(sources = Seq(source))),
      processorLoader = processorLoaderMock(processor)
    ))

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
      delegate,
      stateProviderMock("testrule", Some(RuleState("testrule", Set("1"), new Date(4321), "2", new Date(1234))))
    )

    assert(delegate.sink == Seq(new Date(4321), Collections.singleton("1"), "2", new Date(1234)))
  }

  it should "process the skip variable" in {
    val processor = processorMock(Seq())
    val delegate = spy(new RuleDelegateWithSink(
      configProvider = SimpleConfigProvider(Config(sources = Seq(source))),
      processorLoader = processorLoaderMock(processor),
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
      delegate,
      stateProviderMock("testrule")
    )

    assert(delegate.sink == Seq())
  }

  it should "log the state and custom data" in {
    val processor = processorMock(Seq(), Some(LastSeenData(new Date(1234), "lastSeenId")))
    val stateProvider = stateProviderMock("testrule")
    val delegate = spy(new RuleDelegateWithSink(
      configProvider = SimpleConfigProvider(Config(sources = Seq(source))),
      processorLoader = processorLoaderMock(processor),
    ))
    runRule(
      """
        |rule {
        | ruleName = "testrule"
        | reaction {
        |   memorize.value = 1
        | }
        |}
      """.
        stripMargin,
      delegate,
      stateProvider
    )

    val stateCaptor = ArgumentCaptor.forClass(classOf[State])
    Mockito.verify(stateProvider).logRule(stateCaptor.capture())

    assert(stateCaptor.getValue.customData.contains("value"))
    assert(stateCaptor.getValue.customData("value") == 1)
    assert(stateCaptor.getValue.lastSeenId == "lastSeenId")
    assert(stateCaptor.getValue.lastSeenTimestamp.getTime == new Date(1234).getTime)
  }

  it should "handle overridden target config properly" in {

    val emailTarget = mock[EmailTarget]
    val delegate = spy(new RuleDelegateWithSink(
      configProvider = SimpleConfigProvider(Config(sources = Seq(source))),
      processorLoader = processorLoaderMock(processorMock(Seq())),
      emailTarget = emailTarget
    ))

    runRule(
      """
        rule {
         ruleName = "testrule"
         params = [
             config: [
                     targetSettings: [mailServer: [
                       host: "host",
                       port: "1234",
                       auth: true,
                       username: "username",
                       password: "pass"
                     ]]
             ]
          ]
         reaction {
           email("test@test.com", "sbj", "body")
         }
        }
      """,
      delegate,
      stateProviderMock("testrule")
    )

    Mockito.verify(emailTarget).send("test@test.com", "sbj", "body")(
      Config(
        sources = Seq(source),
        targetSettings = TargetSettings(mailServer = MailServerSettings("host", "1234", true, "username", "pass"))
      )
    )
  }

  it should "handle overridden source config properly" in {
    val loader = processorLoaderMock(processorMock(Seq()))

    val delegate = spy(new RuleDelegateWithSink(
      configProvider = SimpleConfigProvider(Config(sources = Seq(source))),
      processorLoader = loader
    ))

    runRule(
      """
        rule {
         ruleName = "testrule"
         params = [
             config: [
                     sources: [
                             ["url": "new value"]
                     ]
             ]
          ]
         reaction {
           log("test")
         }
        }
      """,
      delegate,
      stateProviderMock("testrule")
    )

    Mockito.verify(loader).load(Source(url = "new value"))
  }

  private def stateProviderMock(rulename: String, state: Option[State] = None) = {
    val stateProvider = mock[StateProvider]
    when(stateProvider.getRuleStatus(rulename)).thenReturn(state)
    stateProvider
  }

  private def source = Source(url = "testSource")

  private def processorMock(messages: Seq[AlertMessage], lastSeenData: Option[LastSeenData] = None): Processor = {
    val processor = mock[Processor]
    when(processor.loadMessages(anyString(), anyObject[Source](), anyObject[Map[String, _]]())).thenReturn(messages)
    when(processor.lastSeenData(anyString(), anyObject[Source](), anyObject[Map[String, _]]())).thenReturn(lastSeenData)
    processor
  }

  private def processorLoaderMock(processor: Processor): ProcessorLoader = {
    val processorLoader = mock[ProcessorLoader]
    when(processorLoader.load(anyObject())).thenReturn(processor)
    processorLoader
  }

  private def runRule(rule: String, delegate: RuleDelegate, stateProvider: StateProvider) = {
    val injector = mock[Injector]
    when(injector.getInstance(classOf[RuleDelegate])).thenReturn(delegate)

    new GroovyRuleRunner(injector).process(
      rule,
      stateProvider
    )
  }
}

object GroovyRuleProcessorSpec {

  class RuleDelegateWithSink(hipChatTarget: HipChatTarget = null,
                             emailTarget: EmailTarget = null,
                             slackTarget: SlackTarget = null,
                             configProvider: ConfigProvider = null,
                             processorLoader: ProcessorLoader = null) extends RuleDelegate(
    hipChatTarget,
    emailTarget,
    slackTarget,
    configProvider,
    processorLoader
  ) {

    val sink: ListBuffer[Any] = ListBuffer[Any]()

    def setSink(value: Any): Unit = sink += value
  }
}