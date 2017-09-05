package lert.core.rule

import java.io.{BufferedReader, InputStream, InputStreamReader}

import com.typesafe.scalalogging.LazyLogging
import lert.core.rule.target.TargetHelper
import groovy.lang.{Binding, GroovyShell}
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ImportCustomizer

trait RuleRunner {
  def process(script: InputStream)
}

class GroovyRuleRunner extends RuleRunner with LazyLogging {
  override def process(script: InputStream): Unit = {
    try {
      val bindings = new Binding() {
        //setVariable("message", JavaUtils.toJava(mes.data))
      }
      val importCustomizer = new ImportCustomizer()
      importCustomizer.addStaticStars(classOf[TargetHelper].getName)

      val config = new CompilerConfiguration() {
        addCompilationCustomizers(importCustomizer)
      }

      val shell = new GroovyShell(this.getClass.getClassLoader, bindings, config)
      shell.evaluate(new BufferedReader(new InputStreamReader(script)))
    } catch {
      case ex: Exception =>
        logger.error(ex.getLocalizedMessage)
        logger.debug(ex.getLocalizedMessage, ex)
    }
  }
}