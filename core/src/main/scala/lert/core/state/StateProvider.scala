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

sealed trait State {
  val ruleName: String = null
  val lastProcessedIds: Set[String] = null
  val lastExecutionTime: Date = null
  val lastSeenId: String = null
  val lastSeenTimestamp: Date = null
  val customData: Map[Any, Any] = Map()
  val mockTargets: Boolean = false
}

@JsonIgnoreProperties(ignoreUnknown = true)
case class RuleState(override val ruleName: String,
                     override val lastProcessedIds: Set[String],
                     override val lastExecutionTime: Date,
                     override val lastSeenId: String,
                     override val lastSeenTimestamp: Date,
                     override val customData: Map[Any, Any] = null) extends State

case class TestRunState(override val mockTargets: Boolean) extends State