package lert

import ch.qos.logback.classic

import scala.collection.JavaConverters._
import com.google.inject.{Guice, Module}
import com.typesafe.scalalogging.LazyLogging
import lert.Core.BASE_PACKAGE
import lert.core.TaskManager
import lert.core.rule.ThreadAppender
import org.reflections.Reflections
import org.slf4j.{Logger, LoggerFactory}

object Application extends App with LazyLogging {
  System.setProperty("withoutRest", "true")
  logger.info("Application starting")
  val taskManager: TaskManager = Guice
    .createInjector(loadAllModules(args).asJava)
    .getInstance(classOf[TaskManager])

  private val root: classic.Logger = LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME).asInstanceOf[classic.Logger]
  root.addAppender(ThreadAppender)
  ThreadAppender.start()

  def loadAllModules(args: Array[String]): Seq[Module] = {
    new Reflections(BASE_PACKAGE).getSubTypesOf(classOf[Module]).asScala
      .filter(_.getPackage.getName.startsWith(BASE_PACKAGE))
      .map { c =>
        logger.info(s"Loading module: $c")
        try {
          c.getConstructor(classOf[Array[String]]).newInstance(args)
        } catch {
          case _: NoSuchMethodException =>
            c.newInstance()
        }
      }.toSeq
  }
}

object Core {
  final val BASE_PACKAGE = "lert"
}