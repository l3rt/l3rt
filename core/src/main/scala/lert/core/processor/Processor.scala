package lert.core.processor

import java.util.Date

import lert.core.config.Source

trait Processor {
  def loadMessages(ruleName: String, source: Source, params: Map[String, Any]): Seq[AlertMessage]

  def lastSeenData(ruleName: String, source: Source, params: Map[String, Any]): Option[LastSeenData]
}

case class AlertMessage(data: Map[String, _])

case class LastSeenData(lastSeenTimestamp: Date,
                        lastSeenId: String)