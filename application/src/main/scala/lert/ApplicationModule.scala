package lert

import scala.collection.JavaConverters._

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.google.inject.Singleton
import com.google.inject.multibindings.Multibinder
import com.typesafe.scalalogging.LazyLogging
import lert.Core.BASE_PACKAGE
import lert.core.cache.{GlobalCache, GuavaCache}
import lert.core.config._
import lert.core.processor.Processor
import lert.core.rule.{GroovyRuleRunner, RuleRunner}
import lert.core.state.{FileStateProvider, StateProvider}
import net.codingwell.scalaguice.ScalaModule
import org.reflections.Reflections

class ApplicationModule extends ScalaModule with LazyLogging {
  private val CONFIG_ENV = "CONFIG"
  private val CONFIG_PROPERTY = "config"

  override def configure(): Unit = {
    bind[RuleRunner].to[GroovyRuleRunner]
    val objectMapper = new ObjectMapper() {
      registerModule(DefaultScalaModule)
    }
    bind[ObjectMapper].toInstance(objectMapper)
    bind[StateProvider].to[FileStateProvider]
    bind[GlobalCache].to[GuavaCache].in[Singleton]

    bind[ConfigParser].to[JsonConfigParser]
    val configFile = Option(System.getenv(CONFIG_ENV))
      .orElse(Option(System.getProperty(CONFIG_PROPERTY)))
      .orNull

    if (configFile != null) {
      bind[ConfigProvider].to[FileConfigProvider]
      bind[String].annotatedWithName("configFile").toInstance(configFile)
    } else if (System.getProperty("config.body") != null) {
      bind[ConfigProvider].toInstance(SimpleConfigProvider(objectMapper.readValue(System.getProperty("config.body"), classOf[Config])))
    } else {
      throw new IllegalStateException("Config is not defined")
    }

    logger.info("Dynamically loaded processors:")

    val multibinder = Multibinder.newSetBinder(binder, classOf[Processor])
    new Reflections(BASE_PACKAGE).getSubTypesOf(classOf[Processor]).asScala.foreach { p =>
      logger.info(s"Processor loaded: $p")
      multibinder.addBinding().to(p)
    }
  }
}
