package lert

import ch.qos.logback.classic
import com.google.inject.{Guice, Module}
import com.typesafe.scalalogging.LazyLogging
import lert.core.Core._
import lert.core.TaskManager
import lert.core.rule.ThreadAppender
import lert.core.utils.ClosableModule
import org.reflections.Reflections
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._

object Application extends App with LazyLogging {
  logger.info("Application starting")
  private val modules = loadAllModules(args)
  private val injector = Guice.createInjector(modules.asJava)
  private val taskManager: TaskManager = injector.getInstance(classOf[TaskManager])

  private val root: classic.Logger = LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME).asInstanceOf[classic.Logger]
  root.addAppender(ThreadAppender)
  ThreadAppender.start()

  private def loadAllModules(args: Array[String]): Seq[Module] = {
    new Reflections(BASE_PACKAGE).getSubTypesOf(classOf[Module]).asScala
      .filter(_.getPackage.getName.startsWith(BASE_PACKAGE))
      .map { c =>
        logger.info(s"Loading module: $c")
        c.newInstance()
      }.toSeq
  }

  def stopApplication(): Unit = {
    modules
      .filter(_.isInstanceOf[ClosableModule])
      .map(_.asInstanceOf[ClosableModule])
      .foreach(_.close(injector))
  }
}