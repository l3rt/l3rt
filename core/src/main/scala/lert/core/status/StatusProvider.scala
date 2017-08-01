package lert.core.status

import java.util.Date

trait StatusProvider {
  def logRule(status: Status): Unit

  def getRuleStatus(ruleName: String): Option[Status]
}

case class Status(ruleName: String, lastProcessedTimestamp: Date, lastProcessedId: String)