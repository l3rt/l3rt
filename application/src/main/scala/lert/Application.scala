package lert

import scala.collection.JavaConverters._

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.google.inject.multibindings.Multibinder
import com.google.inject.{AbstractModule, Guice, Singleton}
import com.typesafe.scalalogging.LazyLogging
import lert.core.cache.{GlobalCache, GuavaCache}
import lert.core.config._
import lert.core.processor.Processor
import lert.core.rule.target.TargetHelper
import lert.core.rule.{GroovyReactionProcessor, ReactionProcessor}
import lert.core.status.{FileStatusProvider, StatusProvider}
import lert.core.{Task, TaskManager}
import net.codingwell.scalaguice.ScalaModule
import org.reflections.Reflections

object Application extends AbstractModule with App with LazyLogging with ScalaModule {
  private val CONFIG_ENV = "CONFIG"

  logger.info("Application starting")
  private val injector = Guice.createInjector(this)
  TargetHelper.setInjector(injector)

  logger.info("IoC container has been loaded")
  private val delay = injector.getInstance(classOf[ConfigProvider]).config.delay
  private val task = injector.getInstance(classOf[Task])
  new TaskManager(if (delay == 0) 5000 else delay, task).start()

  override def configure(): Unit = {
    bind[ReactionProcessor].to[GroovyReactionProcessor]
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
    new Reflections("lert").getSubTypesOf(classOf[Processor]).asScala.foreach { p =>
      logger.info(s"Processor loaded: $p")
      multibinder.addBinding().to(p)
    }
  }
}