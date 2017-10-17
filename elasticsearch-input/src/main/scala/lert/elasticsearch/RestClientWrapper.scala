package lert.elasticsearch

import java.net.URI
import java.util
import java.util.Collections

import com.amazonaws.{ClientConfiguration, DefaultRequest, Request}
import com.amazonaws.auth.{AWS4Signer, BasicAWSCredentials, DefaultAWSCredentialsProviderChain}
import com.amazonaws.http._
import lert.core.config.Source
import lert.elasticsearch.ElasticSearchProcessor.SOURCE_URL_PREFIX
import org.apache.http.entity.BasicHttpEntity
import org.apache.http.{Header, HttpEntity}
import org.elasticsearch.client.RestClient

import scala.collection.JavaConverters._

class RestClientWrapper(source: Source, genericRestClient: RestClient) {

  val isAws = if (source.params.isEmpty) false else source.params.get("isAws").toBoolean

  def performRequest(method: String, endpoint: String, params: util.Map[String, String], entity: HttpEntity, headers: Header*): HttpEntity = {
    if (isAws) {
      val awsRequest = createAwsRequest(method, params, entity, headers: _*)
      signAwsRequest(awsRequest)
      val awsResponse = new AmazonHttpClient(new ClientConfiguration())
        .requestExecutionBuilder()
        .executionContext(new ExecutionContext(true))
        .request(awsRequest)
        .execute()
      val httpEntity = new BasicHttpEntity()
      httpEntity.setContent(awsResponse.getHttpResponse.getContent)
      httpEntity
    } else {
      genericRestClient.performRequest(method, endpoint, params, entity, headers: _*).getEntity
    }
  }

  private def createAwsRequest(method: String, params: util.Map[String, String], entity: HttpEntity, headers: Header*) = {
    val awsRequest = new DefaultRequest[Void]("es")
    awsRequest.setHttpMethod(HttpMethodName.fromValue(method))
    awsRequest.setEndpoint(new URI(source.url.substring(SOURCE_URL_PREFIX.length)))
    awsRequest.setContent(entity.getContent)
    awsRequest.setHeaders(headers.map((header) => header.getName -> header.getValue).toMap.asJava)
    awsRequest.setParameters(params.asScala.map((p) => p._1 -> Collections.singletonList(p._2)).toMap.asJava)
    awsRequest
  }

  private def signAwsRequest(request: Request[Void]) = {
    val region = source.params.get("awsRegion")
    val providedAccessKey = source.params.get("awsAccessKey")
    val providedSecretKey = source.params.get("awsSecretKey")
    val awsCredentials =
      if (providedAccessKey == null || providedSecretKey == null)
        new DefaultAWSCredentialsProviderChain().getCredentials
      else
        new BasicAWSCredentials(providedAccessKey, providedSecretKey)
    val signer = new AWS4Signer
    signer.setRegionName(region)
    signer.setServiceName("es")
    signer.sign(request, awsCredentials)
  }
}
