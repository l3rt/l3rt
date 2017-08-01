package lert.core.rule

import java.io.{BufferedReader, InputStream, InputStreamReader}

import lert.core.rule.target.TargetHelper
import groovy.lang.{Binding, GroovyShell}
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ImportCustomizer

trait ReactionProcessor {
  def process(script: InputStream)
}

class GroovyReactionProcessor extends ReactionProcessor {
  override def process(script: InputStream): Unit = {
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
  }
}