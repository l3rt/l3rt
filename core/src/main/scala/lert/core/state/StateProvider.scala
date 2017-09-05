package lert.core.state

import java.util.Date

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

/**
  * Stores historical data of rule execution.
  */
trait StateProvider {
  def logRule(status: State): Unit

  def getRuleStatus(ruleName: String): Option[State]
}

@JsonIgnoreProperties(ignoreUnknown = true)
case class State(ruleName: String,
                 lastProcessedIds: Set[String],
                 lastExecutionTime: Date,
                 lastSeenId: String,
                 lastSeenTimestamp: Date,
                 customData: Map[Any, Any] = null)