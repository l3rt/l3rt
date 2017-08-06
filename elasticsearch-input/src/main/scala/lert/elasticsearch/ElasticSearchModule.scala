package lert.elasticsearch

import scala.collection.JavaConverters._

import com.google.inject.multibindings.Multibinder
import com.typesafe.scalalogging.LazyLogging
import lert.elasticsearch.matcher.Matcher
import net.codingwell.scalaguice.ScalaModule
import org.reflections.Reflections

class ElasticSearchModule extends ScalaModule with LazyLogging {
  override def configure(): Unit = {
    val multibinder = Multibinder.newSetBinder(binder, classOf[Matcher])
    new Reflections("lert").getSubTypesOf(classOf[Matcher]).asScala.foreach { c =>
      logger.info(s"Matcher loaded: $c")
      multibinder.addBinding().to(c)
    }
  }
}
