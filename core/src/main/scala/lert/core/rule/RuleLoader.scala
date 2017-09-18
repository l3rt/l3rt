package lert.core.rule

import java.nio.file.{Files, Paths}

import scala.collection.JavaConverters._
import com.google.inject.Inject
import lert.core.state.StateProvider

class RuleLoader @Inject()(ruleRunner: RuleRunner, ruleSource: RuleSource, stateProvider: StateProvider) {
  def process(rule: String): Unit = {
    ruleSource.load(rule).foreach { case Rule(_, script) => ruleRunner.process(script, stateProvider) }
  }
}

case class Rule(id: String, script: String)

trait RuleSource {
  def load(location: String): Seq[Rule]
}

class FolderRuleSource extends RuleSource {
  override def load(location: String): Seq[Rule] = {
    val path = Paths.get(location)
    Files
      .newDirectoryStream(path)
      .asScala
      .map { p =>
        Rule(path.relativize(p).toString, new String(Files.readAllBytes(p)))
      }.toSeq
  }
}