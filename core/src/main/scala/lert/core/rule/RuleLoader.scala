package lert.core.rule

import java.io.ByteArrayInputStream
import java.nio.file.{Files, Paths}

import com.google.inject.Inject

class RuleLoader @Inject()(ruleProcessor: ReactionProcessor) {
  def process(rule: String): Unit = {
    if (rule.startsWith("classpath:")) {
      new ClasspathRule(rule).processMessages(ruleProcessor)

    } else if (rule.startsWith("/")) {
      new FileRule(rule).processMessages(ruleProcessor)

    } else {
      throw new IllegalArgumentException(s"A rule loader for $rule is not found")
    }
  }
}

trait Rule {
  def processMessages(ruleProcessor: ReactionProcessor)
}

class ClasspathRule(filePath: String) extends Rule {
  override def processMessages(ruleProcessor: ReactionProcessor): Unit = {
    ruleProcessor.process(this.getClass.getClassLoader.getResourceAsStream(filePath))
  }
}

class FileRule(filePath: String) extends Rule {
  override def processMessages(ruleProcessor: ReactionProcessor): Unit = {
    val bytes = Files.readAllBytes(Paths.get(filePath))
    ruleProcessor.process(new ByteArrayInputStream(bytes))
  }
}