package lert.core

import java.util
import javax.inject.Inject

import lert.core.config.Source

import scala.collection.JavaConverters._
import lert.core.processor.Processor

class ProcessorLoader @Inject()(processors: util.Set[Processor]) {
  def load(source: Source): Processor =
    processors.asScala.find(_.supports(source))
      .getOrElse(throw new IllegalStateException(s"No processors found for $source. Available processors are: ${processors.toString}"))

}
