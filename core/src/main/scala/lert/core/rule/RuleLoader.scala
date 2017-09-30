package lert.core.rule

import java.nio.file.{Files, Paths}

import com.google.inject.Inject
import com.typesafe.scalalogging.LazyLogging
import lert.core.state.StateProvider
import org.slf4j.MDC

import scala.collection.JavaConverters._

class RuleLoader @Inject()(ruleRunner: RuleRunner, ruleSource: RuleSource, stateProvider: StateProvider) extends LazyLogging {
  def process(rule: String): Unit = {
    ruleSource
      .load(rule)
      .foreach { case Rule(id, script) =>
        MDC.put("ruleId", id)

        try ruleRunner.process(script, stateProvider)
        catch {
          case ex: Exception =>
            logger.error(ex.getLocalizedMessage)
        }

        MDC.clear()
      }
  }
}

case class Rule(id: String, script: String)

trait RuleSource {
  def save(location: String, rule: Rule): Unit = throw new NotImplementedError(s"${this.getClass.getSimpleName} doesn't support rule saving")

  def load(location: String): Seq[Rule]
}

class FolderRuleSource extends RuleSource {

  override def save(location: String, rule: Rule): Unit = {
    val path = Paths.get(location)
    if (Files.isDirectory(path)) {
      Files.write(Paths.get(location, rule.id), Option(rule.script).getOrElse("").getBytes)
    } else {
      throw new IllegalAccessException(s"Couldn't save the rule to $location because it's not a folder")
    }
  }

  override def load(location: String): Seq[Rule] = {
    val path = Paths.get(location)
    if (Files.isDirectory(path)) {
      Files
        .newDirectoryStream(path)
        .asScala
        .map { p =>
          Rule(path.relativize(p).toString, new String(Files.readAllBytes(p)))
        }.toSeq
    } else {
      Seq(Rule(path.getFileName.toString, new String(Files.readAllBytes(path))))
    }
  }
}