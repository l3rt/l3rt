package lert.elasticsearch

import java.net.URI
import java.util
import java.util.Collections

import com.amazonaws.{AmazonServiceException, ClientConfiguration, DefaultRequest, Request}
import com.amazonaws.auth.{AWS4Signer, BasicAWSCredentials, DefaultAWSCredentialsProviderChain}
import com.amazonaws.http._
import com.amazonaws
import lert.core.config.Source
import lert.elasticsearch.ElasticSearchProcessor.SOURCE_URL_PREFIX
import lert.elasticsearch.ElasticSearchProcessorUtils._
import org.apache.http.{Header, HttpEntity, HttpHost}
import org.elasticsearch.client.RestClient


import scala.collection.JavaConverters._

trait CustomRestClient {
  def performRequest(method: String, endpoint: String, params: util.Map[String, String], entity: HttpEntity, headers: Header*): ElasticJsonResponse
}

object CustomRestClient {
  def apply(implicit source: Source): CustomRestClient = {
    val isAws: Boolean = if (source.params.isEmpty) false else source.params.get("isAws").toBoolean
    if (isAws)
      new AwsClient
    else
      new GenericElasticSearchClient
  }
}

class GenericElasticSearchClient(implicit source: Source) extends CustomRestClient {
  val restClient = RestClient.builder(HttpHost.create(source.url.substring(SOURCE_URL_PREFIX.length))).build()

  override def performRequest(method: String, endpoint: String, params: util.Map[String, String], entity: HttpEntity, headers: Header*): ElasticJsonResponse = {
    val content = restClient.performRequest(method, endpoint, params, entity, headers: _*).getEntity.getContent
    new ElasticJsonResponse(convertInputStreamToStringAndClose(content))
  }
}

class AwsClient(implicit source: Source) extends CustomRestClient {
  val requestExecutionBuilder = new AmazonHttpClient(new ClientConfiguration())
    .requestExecutionBuilder()
    .errorResponseHandler(new CustomErrorHandler())

  override def performRequest(method: String, endpoint: String, params: util.Map[String, String], entity: HttpEntity, headers: Header*): ElasticJsonResponse = {
    val awsRequest = createAwsRequest(method, endpoint, params, entity, headers: _*)
    signAwsRequest(awsRequest)
    val awsResponse: amazonaws.Response[ElasticJsonResponse] = requestExecutionBuilder.request(awsRequest).execute(new CustomHttpResponseHandler)
    awsResponse.getAwsResponse
  }

  private def createAwsRequest(method: String, resourcePath: String, params: util.Map[String, String], entity: HttpEntity, headers: Header*) = {
    val awsRequest = new DefaultRequest[Void]("es")
    awsRequest.setHttpMethod(HttpMethodName.fromValue(method))
    awsRequest.setEndpoint(new URI(source.url.substring(SOURCE_URL_PREFIX.length)))
    awsRequest.setResourcePath(resourcePath)
    awsRequest.setContent(entity.getContent)
    awsRequest.setHeaders(headers.map(header => header.getName -> header.getValue).toMap.asJava)
    awsRequest.setParameters(params.asScala.map(param => param._1 -> Collections.singletonList(param._2)).asJava)
    awsRequest
  }

  private def signAwsRequest(request: Request[Void]): Unit = {
    val region = source.params.get("awsRegion")
    if (region == null || region.isEmpty) {
      throw new RuntimeException("AWS region must be provided.")
    }
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

  private class CustomHttpResponseHandler extends HttpResponseHandler[ElasticJsonResponse] {
    override def handle(response: HttpResponse): ElasticJsonResponse = {
      new ElasticJsonResponse(convertInputStreamToStringAndClose(response.getContent))
    }

    override def needsConnectionLeftOpen = false
  }

  private class CustomErrorHandler extends HttpResponseHandler[AmazonServiceException] {
    override def handle(response: HttpResponse): AmazonServiceException = {
      val ex = new AmazonServiceException("Error occurred on attempt to get data from ElasticSearch")
      ex.setStatusCode(response.getStatusCode)
      ex.setErrorCode(response.getStatusText)
      ex
    }

    override def needsConnectionLeftOpen = false
  }
}
