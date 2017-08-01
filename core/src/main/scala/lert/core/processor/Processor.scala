package lert.core.processor

import lert.core.config.Source

trait Processor {
  def loadMessages(ruleName: String, source: Source, params: Map[String, Any]): Seq[AlertMessage]
}

case class AlertMessage(data: Map[String, _])