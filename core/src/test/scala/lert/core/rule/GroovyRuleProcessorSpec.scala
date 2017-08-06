package lert.core.rule

import java.io.ByteArrayInputStream

import lert.core.config.{Config, SimpleConfigProvider, Source}
import lert.core.processor.{AlertMessage, Processor}
import lert.core.rule.target.{EmailTarget, HipChatTarget, TargetHelper}
import lert.core.{BaseSpec, ProcessorLoader}
import com.google.inject.Injector
import groovy.lang.Closure
import org.mockito.Matchers._
import org.mockito.Mockito._

class GroovyRuleProcessorSpec extends BaseSpec {
  it should "run the script" in {
    val processor = processorMock(Seq(AlertMessage(Map("test" -> Map("1" -> "2")))))
    val delegate = spy(new RuleDelegate(null, null, null, SimpleConfigProvider(Config(sources = Seq(source))), processorLoaderMock(processor)))
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
    val delegate = spy(new RuleDelegate(null, null, null, SimpleConfigProvider(Config(sources = Seq(source))), processorLoaderMock(processor)))
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
    val delegate = spy(new RuleDelegate(null, null, null, SimpleConfigProvider(Config(sources = Seq(source))), processorLoaderMock(processor)))
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
    val hipchatTarget = mock(classOf[HipChatTarget])
    val delegate = spy(new RuleDelegate(hipchatTarget, null, null, SimpleConfigProvider(Config(sources = Seq(source))), processorLoaderMock(processor)))
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

    verify(hipchatTarget).send("room", "message", "color", true)
  }

  it should "send message via email" in {
    val processor = processorMock(Seq())
    val emailTarget = mock(classOf[EmailTarget])
    val delegate = spy(new RuleDelegate(null, emailTarget, null, SimpleConfigProvider(Config(sources = Seq(source))), processorLoaderMock(processor)))
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

  private def source = Source("test", "testSource", Map())

  private def processorMock(messages: Seq[AlertMessage]): Processor = {
    val processor = mock(classOf[Processor])
    when(processor.loadMessages(anyString(), anyObject[Source](), anyObject[Map[String, _]]())).thenReturn(messages)
    processor
  }

  private def processorLoaderMock(processor: Processor): ProcessorLoader = new ProcessorLoader(null) {
    override def load(sourceType: String): Processor = processor
  }

  private def runRule(rule: String, delegate: RuleDelegate) = {
    val injector = mock(classOf[Injector])
    TargetHelper.setInjector(injector)
    when(injector.getInstance(classOf[RuleDelegate])).thenReturn(delegate)

    new GroovyRuleRunner().process(new ByteArrayInputStream(
      rule.getBytes()
    ))
  }
}
