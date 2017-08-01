package lert.core

import java.util
import javax.inject.Inject

import scala.collection.JavaConverters._

import lert.core.processor.Processor

class ProcessorLoader @Inject()(processors: util.Set[Processor]) {
  def load(sourceType: String): Processor =
    processors.asScala.find(_.getClass.getName == sourceType)
      .getOrElse(throw new IllegalStateException(s"Processor $sourceType is not found. Available processors are: ${processors.toString}"))

}
