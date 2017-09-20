
package lert.rest

import java.util.Date
import javax.inject.Inject

import com.fasterxml.jackson.databind.ObjectMapper
import com.typesafe.scalalogging.LazyLogging
import io.undertow.server.handlers.resource.{ClassPathResourceManager, ResourceHandler}
import io.undertow.server.{HttpHandler, HttpServerExchange}
import io.undertow.util.{Headers, HttpString}
import lert.core.config.ArgumentProvider
import lert.core.rule.{RuleRunner, RuleSource, ThreadAppender}
import lert.core.state.{StateProvider, StaticStateProvider, TestRunState}

class Controller @Inject()(objectMapper: ObjectMapper,
                           ruleSource: RuleSource,
                           argumentProvider: ArgumentProvider,
                           ruleRunner: RuleRunner,
                           stateProvider: StateProvider) extends HttpHandler with LazyLogging {

  override def handleRequest(exchange: HttpServerExchange): Unit = {
    setupCORS(exchange)

    val reqPath = exchange.getRequestPath
    val method = exchange.getRequestMethod.toString

    if (reqPath == "/rules" && (method == "GET" || method == "OPTIONS")) {
      exchange.getResponseSender.send(ruleSource.load(argumentProvider.arguments.rules).asJson)
      exchange.getResponseHeaders.put(Headers.CONTENT_TYPE, "application/json")
    } else if (reqPath == "/runScript") {
      if (method == "OPTIONS") {
        exchange.getResponseSender.close()
      } else if (method == "POST") {
        exchange.getResponseHeaders.put(Headers.CONTENT_TYPE, "application/json")
        exchange.getRequestReceiver.receiveFullString((exchange: HttpServerExchange, message: String) => {

          val request = objectMapper.readValue(message, classOf[RunScriptRequest])
          ThreadAppender.startCapturing()
          try {
            ruleRunner.process(request.script, new StaticStateProvider(TestRunState(request.mockTargets)))
          } catch {
            case ex: Exception =>
              logger.error(ex.getLocalizedMessage)
              logger.debug(ex.getLocalizedMessage, ex)
          } finally {
            exchange.getResponseSender.send(ExecutionResult(
              ThreadAppender.getAndClear().map(e =>
                LogEvent(new Date(e.getTimeStamp), e.getLevel.toString, e.getFormattedMessage)
              )
            ).asJson)
          }
        })
      }
    } else if (reqPath == "/") {
      exchange.setStatusCode(302)
      exchange.getResponseHeaders.add(new HttpString("Location"), "/public/index.html")
      exchange.getResponseSender.close()
    } else if (reqPath.startsWith("/")) {
      new ResourceHandler(new ClassPathResourceManager(this.getClass.getClassLoader)).handleRequest(exchange)
    } else {
      exchange.setStatusCode(404)
      exchange.getResponseSender.send("Not found!")
    }
  }

  private def setupCORS(exchange: HttpServerExchange) = {
    exchange.getResponseHeaders
      .put(new HttpString("Access-Control-Allow-Origin"), "*")
      .put(new HttpString("Access-Control-Allow-Methods"), "POST, GET, OPTIONS")
      .put(new HttpString("Access-Control-Allow-Headers"), "content-type")
  }

  implicit class Json(obj: Any) {
    def asJson = objectMapper.writeValueAsString(obj)
  }

}

case class RunScriptRequest(script: String, mockTargets: Boolean)

case class ExecutionResult(log: Seq[LogEvent])

case class LogEvent(time: Date, level: String, message: String)