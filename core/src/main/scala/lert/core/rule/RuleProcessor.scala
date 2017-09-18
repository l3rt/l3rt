package lert.core.rule

import javax.inject.Inject

import com.google.inject.Injector
import com.typesafe.scalalogging.LazyLogging
import groovy.lang.{Binding, Closure, GroovyShell}
import lert.core.state.StateProvider
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ImportCustomizer

trait RuleRunner {
  def process(script: String, stateProvider: StateProvider): Unit
}

class GroovyRuleRunner @Inject()(injector: Injector) extends RuleRunner with LazyLogging {
  override def process(script: String, stateProvider: StateProvider): Unit = {

    val bindings = new Binding() {
      setVariable("rule", new RuleStarter {
        override def call(cl: Closure[Unit]): Unit = {
          val instance = injector.getInstance(classOf[RuleDelegate])
          instance.stateProvider = stateProvider
          cl.setDelegate(instance)
          cl.setResolveStrategy(Closure.DELEGATE_FIRST)
          cl.call
          if (!instance.reactionWasCalled) throw new IllegalStateException("Reaction hasn't been called. Please ensure that you have a 'reaction {...}' block in your rule.")
        }
      })
    }

    val importCustomizer = new ImportCustomizer()
    //importCustomizer.addStaticStars(classOf[TargetHelper].getName)

    val config = new CompilerConfiguration() {
      addCompilationCustomizers(importCustomizer)
    }

    val shell = new GroovyShell(this.getClass.getClassLoader, bindings, config)
    shell.evaluate(script)
  }
}

trait RuleStarter {
  def call(cl: Closure[Unit])
}