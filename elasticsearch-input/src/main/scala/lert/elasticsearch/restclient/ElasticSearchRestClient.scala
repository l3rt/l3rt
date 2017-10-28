package lert.elasticsearch.restclient

import lert.core.config.Source
import lert.elasticsearch.ElasticSearchProcessor.SOURCE_URL_PREFIX
import org.apache.http.{Header, HttpEntity, HttpHost}
import org.elasticsearch.client.{RestClient => ESRestClient}

import scala.collection.JavaConverters._

class ElasticSearchRestClient(source: Source) extends RestClient {
  private val restClient: ESRestClient = ESRestClient.builder(HttpHost.create(source.url.substring(SOURCE_URL_PREFIX.length))).build()

  override def performRequest(method: String, endpoint: String, params: Map[String, String], entity: HttpEntity, headers: Header*): Response = {
    val response = restClient.performRequest(method, endpoint, params.asJava, entity, headers: _*)
    Response(response.getEntity.getContent, response.getStatusLine.getStatusCode)
  }

  override def supports(source: Source): Boolean = true
}
