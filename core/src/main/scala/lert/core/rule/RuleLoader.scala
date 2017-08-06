package lert.core.rule

import java.io.ByteArrayInputStream
import java.nio.file.{Files, Paths}

import com.google.inject.Inject

class RuleLoader @Inject()(ruleProcessor: RuleRunner) {
  def process(rule: String): Unit = {
    if (rule.startsWith("classpath:")) {
      new ClasspathRule(rule).processMessages(ruleProcessor)

    } else if (rule.startsWith("/")) {
      val path = Paths.get(rule)
      if (Files.isDirectory(path)) {
        new FolderRule(rule).processMessages(ruleProcessor)
      } else {
        new FileRule(rule).processMessages(ruleProcessor)
      }

    } else {
      throw new IllegalArgumentException(s"A rule loader for $rule is not found")
    }
  }
}

trait Rule {
  def processMessages(ruleProcessor: RuleRunner)
}

class ClasspathRule(filePath: String) extends Rule {
  override def processMessages(ruleProcessor: RuleRunner): Unit = {
    ruleProcessor.process(this.getClass.getClassLoader.getResourceAsStream(filePath))
  }
}

class FileRule(filePath: String) extends Rule {
  override def processMessages(ruleProcessor: RuleRunner): Unit = {
    val bytes = Files.readAllBytes(Paths.get(filePath))
    ruleProcessor.process(new ByteArrayInputStream(bytes))
  }
}

class FolderRule(folderPath: String) extends Rule {
  override def processMessages(ruleProcessor: RuleRunner): Unit = {
    Files.newDirectoryStream(Paths.get(folderPath)).forEach { p =>
      val bytes = Files.readAllBytes(p)
      ruleProcessor.process(new ByteArrayInputStream(bytes))
    }
  }
}