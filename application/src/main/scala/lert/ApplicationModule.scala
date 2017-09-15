package lert

import scala.collection.JavaConverters._

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.google.inject.{Inject, Injector, Provides, Singleton}
import com.google.inject.multibindings.Multibinder
import com.typesafe.scalalogging.LazyLogging
import lert.Core.BASE_PACKAGE
import lert.core.TaskManager
import lert.core.cache.{GlobalCache, GuavaCache}
import lert.core.config._
import lert.core.processor.Processor
import lert.core.rule.target.TargetHelper
import lert.core.rule.{FolderRuleSource, GroovyRuleRunner, RuleRunner, RuleSource}
import lert.core.state.{FileStateProvider, StateProvider}
import net.codingwell.scalaguice.ScalaModule
import org.reflections.Reflections

class ApplicationModule(args: Array[String]) extends ScalaModule with LazyLogging {

  override def configure(): Unit = {
    bind[RuleRunner].to[GroovyRuleRunner]
    if (Option(System.getProperty("withoutRest")).exists(_.toBoolean)) {
      val objectMapper = new ObjectMapper() {
        registerModule(DefaultScalaModule)
      }
      bind[ObjectMapper].toInstance(objectMapper)
    }
    bind[StateProvider].to[FileStateProvider]
    bind[GlobalCache].to[GuavaCache].in[Singleton]

    bind[Array[String]].annotatedWithName("args").toInstance(args)
    bind[ConfigParser].to[JsonConfigParser]

    bind[ConfigProvider].to[FileConfigProvider]
    bind[RuleSource].to[FolderRuleSource]

    logger.info("Dynamically loaded processors:")

    val multibinder = Multibinder.newSetBinder(binder, classOf[Processor])
    new Reflections(BASE_PACKAGE).getSubTypesOf(classOf[Processor]).asScala.foreach { p =>
      logger.info(s"Processor loaded: $p")
      multibinder.addBinding().to(p)
    }

    requestInjection(this)
  }

  @Inject def initTaskManager(instance: TaskManager): Unit = {
    instance.start()
  }

  @Inject def targetHelper(injector: Injector): Unit = {
    TargetHelper.setInjector(injector)
  }
}
