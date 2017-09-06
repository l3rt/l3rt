package lert

import scala.collection.JavaConverters._

import com.google.inject.{Guice, Module}
import com.typesafe.scalalogging.LazyLogging
import lert.Core.BASE_PACKAGE
import lert.core.config._
import lert.core.rule.target.TargetHelper
import lert.core.{Task, TaskManager}
import org.reflections.Reflections

object Application extends App with LazyLogging {
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
  val taskManager = new TaskManager(if (delay == 0) 5000 else delay, task)
  taskManager.start()
}

object Core {
  final val BASE_PACKAGE = "lert"
}