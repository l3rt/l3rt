package lert.elasticsearch.matcher

import lert.core.processor.AlertMessage
import lert.elasticsearch.CustomRestClient

trait Matcher {
  val MATCHER_PARAMETER = "matcher"

  def supports(params: Map[String, _]): Boolean

  def query(ruleName: String, client: CustomRestClient, params: Map[String, _]): Seq[AlertMessage]
}
