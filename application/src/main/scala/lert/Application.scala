package lert

import scala.collection.JavaConverters._

import com.google.inject.{Guice, Module}
import com.typesafe.scalalogging.LazyLogging
import lert.Core.BASE_PACKAGE
import lert.core.TaskManager
import org.reflections.Reflections

object Application extends App with LazyLogging {
  System.setProperty("withoutRest", "true")
  logger.info("Application starting")
  val taskManager: TaskManager = Guice
    .createInjector(loadAllModules(args).asJava)
    .getInstance(classOf[TaskManager])

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