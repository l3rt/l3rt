package lert

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.google.inject.multibindings.Multibinder
import com.google.inject.{Inject, Injector, Singleton}
import com.typesafe.scalalogging.LazyLogging
import lert.Application.injector
import lert.Core.BASE_PACKAGE
import lert.core.TaskManager
import lert.core.cache.{GlobalCache, GuavaCache}
import lert.core.config._
import lert.core.processor.Processor
import lert.core.rule.{FolderRuleSource, GroovyRuleRunner, RuleRunner, RuleSource}
import lert.core.state.{FileStateProvider, StateProvider}
import lert.core.utils.ClosableModule
import net.codingwell.scalaguice.ScalaModule
import org.reflections.Reflections

import scala.collection.JavaConverters._

class ApplicationModule(args: Array[String]) extends ScalaModule with LazyLogging with ClosableModule {

  override def configure(): Unit = {
    bind[RuleRunner].to[GroovyRuleRunner]
    val objectMapper = new ObjectMapper() {
      registerModule(DefaultScalaModule)
    }
    bind[ObjectMapper].toInstance(objectMapper)
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

  override def close(injector: Injector): Unit = {
    injector.getInstance(classOf[TaskManager]).stop()
  }
}
