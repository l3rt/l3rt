package lert.rest

import javax.inject.Inject

import com.fasterxml.jackson.databind.ObjectMapper
import io.undertow.server.{HttpHandler, HttpServerExchange}
import io.undertow.util.{Headers, HttpString}
import lert.core.config.ArgumentProvider
import lert.core.rule.{RuleRunner, RuleSource}

class Controller @Inject()(objectMapper: ObjectMapper,
                           ruleSource: RuleSource,
                           argumentProvider: ArgumentProvider,
                           ruleRunner: RuleRunner) extends HttpHandler {

  override def handleRequest(exchange: HttpServerExchange) = {
    setupCORS(exchange)
    exchange.getResponseHeaders.put(Headers.CONTENT_TYPE, "application/json")
    val reqPath = exchange.getRequestPath
    val method = exchange.getRequestMethod.toString

    if (reqPath == "/rules" && (method == "GET" || method == "OPTIONS")) {
      exchange.getResponseSender.send(ruleSource.load(argumentProvider.arguments.rules).asJson)
    } else if (reqPath == "/runScript") {
      if (method == "OPTIONS") {
        exchange.getResponseSender.close()
      } else if (method == "POST") {
        exchange.getRequestReceiver.receiveFullString((exchange: HttpServerExchange, message: String) => {
          val script = objectMapper.readValue(message, classOf[RunScriptRequest]).script
          try {
            ruleRunner.process(script)
            exchange.getResponseSender.send(ExecutionResult("OK").asJson)
          } catch {
            case ex: Exception =>
              exchange.getResponseSender.send(ExecutionResult(ex.getLocalizedMessage).asJson)
          }
        })
      }
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

case class RunScriptRequest(script: String)

case class ExecutionResult(log: String)

//
//class Service @Inject()(ruleRunner: RuleRunner) extends LazyLogging {
//  implicit val strategy = Strategy.fromFixedDaemonPool(8, "fs2")
//
//  def runScript(script: String): Task[ExecutionResult] = {
//    logger.info(Thread.currentThread().getName)
//    Task {
//      logger.info(Thread.currentThread().getName)
//      ruleRunner.process(script)
//      ExecutionResult("OK")
//    }
//  }
//}
//
//