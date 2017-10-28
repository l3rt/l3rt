package lert.elasticsearch.restclient

import java.net.URI
import java.util.Collections

import com.amazonaws.auth.{AWS4Signer, BasicAWSCredentials, DefaultAWSCredentialsProviderChain}
import com.amazonaws.http.{AmazonHttpClient, HttpMethodName, HttpResponse, HttpResponseHandler}
import com.amazonaws.{AmazonServiceException, ClientConfiguration, DefaultRequest}
import lert.core.config.Source
import lert.elasticsearch.ElasticSearchProcessor.SOURCE_URL_PREFIX
import lert.elasticsearch.restclient.AWSRestClient._
import org.apache.http.{Header, HttpEntity}

import scala.collection.JavaConverters._

class AWSRestClient(source: Source) extends RestClient {
  private val requestExecutionBuilder = new AmazonHttpClient(new ClientConfiguration())
    .requestExecutionBuilder()
    .errorResponseHandler(new CustomErrorHandler())

  override def performRequest(method: String, endpoint: String, params: Map[String, String], entity: HttpEntity, headers: Header*): Response =
    requestExecutionBuilder.request(
      createSignedAwsRequest(method, endpoint, params, entity, headers: _*)
    ).execute(new HttpResponseHandler[Response] {
      override def handle(response: HttpResponse): Response = Response(response.getContent, response.getStatusCode)
      override val needsConnectionLeftOpen = false
    }).getAwsResponse

  private def createAwsRequest(method: String, resourcePath: String, params: Map[String, String], entity: HttpEntity, headers: Header*) =
    new DefaultRequest[Void](SERVICE_NAME) {
      setHttpMethod(HttpMethodName.fromValue(method))
      setEndpoint(new URI(source.url.substring(SOURCE_URL_PREFIX.length)))
      setResourcePath(resourcePath)
      setContent(entity.getContent)
      setHeaders(headers.map(header => header.getName -> header.getValue).toMap.asJava)
      setParameters(params.map(param => param._1 -> Collections.singletonList(param._2)).asJava)
    }

  private def createSignedAwsRequest(method: String, resourcePath: String, params: Map[String, String], entity: HttpEntity, headers: Header*): DefaultRequest[_] = {
    val sourceParams = source.params.getOrElse(throw new RuntimeException("Source parameters are not specified"))
    val region = sourceParams(PARAM_AWS_REGION)

    val awsCredentials =
      (sourceParams.get(PARAM_AWS_ACCESS_KEY), sourceParams.get(PARAM_AWS_SECRET_KEY)) match {

        case (Some(accessKey), Some(secretKey)) =>
          new BasicAWSCredentials(accessKey, secretKey)

        case _ =>
          new DefaultAWSCredentialsProviderChain().getCredentials
      }

    val signer = new AWS4Signer
    signer.setRegionName(region)
    signer.setServiceName(SERVICE_NAME)
    val request = createAwsRequest(method, resourcePath, params, entity, headers: _*)
    signer.sign(request, awsCredentials)
    request
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

  override def supports(source: Source): Boolean = source.params.exists(_.contains(PARAM_AWS_REGION))
}

object AWSRestClient {
  val SERVICE_NAME = "es"
  val PARAM_AWS_REGION = "awsRegion"
  val PARAM_AWS_SECRET_KEY = "awsSecretKey"
  val PARAM_AWS_ACCESS_KEY = "awsAccessKey"
}