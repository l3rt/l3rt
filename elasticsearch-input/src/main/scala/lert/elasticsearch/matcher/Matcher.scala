package lert.elasticsearch.matcher

import lert.core.processor.AlertMessage
import lert.core.state.State
import org.elasticsearch.client.RestClient

trait Matcher {
  val MATCHER_PARAMETER = "matcher"

  def supports(params: Map[String, _]): Boolean

  def query(client: RestClient, params: Map[String, _]): Seq[AlertMessage]
}
