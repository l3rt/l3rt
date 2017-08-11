package lert.core.status

import java.util.Date

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

trait StatusProvider {
  def logRule(status: Status): Unit

  def getRuleStatus(ruleName: String): Option[Status]
}

@JsonIgnoreProperties(ignoreUnknown = true)
case class Status(ruleName: String,
                  lastProcessedIds: Set[String],
                  lastSeenTimestamp: Date,
                  lastSeenId: String,
                  lastExecutionTime: Date)