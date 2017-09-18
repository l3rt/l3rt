package lert.core.state

class StaticStateProvider(state: State) extends StateProvider {
  override def logRule(status: State): Unit = {}

  override def getRuleStatus(ruleName: String): Option[State] = Option(state)
}