package lert.core.rule

import com.typesafe.scalalogging.LazyLogging
import groovy.lang.{Binding, GroovyShell}
import lert.core.rule.target.TargetHelper
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ImportCustomizer

trait RuleRunner {
  def process(script: String)
}

class GroovyRuleRunner extends RuleRunner with LazyLogging {
  override def process(script: String): Unit = {
    val bindings = new Binding() {
      //setVariable("message", JavaUtils.toJava(mes.data))
    }
    val importCustomizer = new ImportCustomizer()
    importCustomizer.addStaticStars(classOf[TargetHelper].getName)

    val config = new CompilerConfiguration() {
      addCompilationCustomizers(importCustomizer)
    }

    val shell = new GroovyShell(this.getClass.getClassLoader, bindings, config)
    shell.evaluate(script)
  }
}