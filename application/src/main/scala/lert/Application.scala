package lert

import scala.collection.JavaConverters._

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.google.inject.multibindings.Multibinder
import com.google.inject.{Guice, Module, Singleton}
import com.typesafe.scalalogging.LazyLogging
import lert.Application.BASE_PACKAGE
import lert.core.cache.{GlobalCache, GuavaCache}
import lert.core.config._
import lert.core.processor.Processor
import lert.core.rule.target.TargetHelper
import lert.core.rule.{GroovyRuleRunner, RuleRunner}
import lert.core.status.{FileStatusProvider, StatusProvider}
import lert.core.{Task, TaskManager}
import net.codingwell.scalaguice.ScalaModule
import org.reflections.Reflections

object Application extends App with LazyLogging {
  val BASE_PACKAGE = "lert"

  logger.info("Application starting")
  private val injector = Guice.createInjector(
    new Reflections(BASE_PACKAGE).getSubTypesOf(classOf[Module]).asScala
      .filter(_.getPackage.getName.startsWith(BASE_PACKAGE))
      .map { c =>
        logger.info(s"Loading module: $c")
        c.newInstance()
      }.asJava
  )
  TargetHelper.setInjector(injector)

  logger.info("IoC container has been loaded")
  private val delay = injector.getInstance(classOf[ConfigProvider]).config.delay
  private val task = injector.getInstance(classOf[Task])
  new TaskManager(if (delay == 0) 5000 else delay, task).start()
}

class ApplicationModule extends ScalaModule with LazyLogging {
  private val CONFIG_ENV = "CONFIG"

  override def configure(): Unit = {
    bind[RuleRunner].to[GroovyRuleRunner]
    bind[ObjectMapper].toInstance(new ObjectMapper() {
      registerModule(DefaultScalaModule)
    })
    bind[StatusProvider].to[FileStatusProvider]
    bind[GlobalCache].to[GuavaCache].in[Singleton]

    bind[ConfigParser].to[JsonConfigParser]
    Option(System.getenv(CONFIG_ENV))
      .orElse(throw new IllegalStateException("Config is not defined"))
      .foreach { configLocation =>
        bind[ConfigProvider].to[FileConfigProvider]
        bind[String].annotatedWithName("configFile").toInstance(configLocation)
      }

    logger.info("Dynamically loaded processors:")

    val multibinder = Multibinder.newSetBinder(binder, classOf[Processor])
    new Reflections(BASE_PACKAGE).getSubTypesOf(classOf[Processor]).asScala.foreach { p =>
      logger.info(s"Processor loaded: $p")
      multibinder.addBinding().to(p)
    }
  }
}