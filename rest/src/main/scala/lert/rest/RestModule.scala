package lert.rest

import com.google.inject.Inject
import com.typesafe.scalalogging.LazyLogging
import io.undertow.Undertow
import net.codingwell.scalaguice.ScalaModule

class RestModule extends ScalaModule {

  override def configure(): Unit = {
    requestInjection(this)
  }

  @Inject def initTaskManager(instance: Server): Unit = {
    instance.start()
  }
}

class Server @Inject()(controller: Controller) extends LazyLogging {
  def start() = {
    logger.info("HTTP server is being started")

    val server = Undertow.builder
      .addHttpListener(8080, "localhost", controller)
      .build

    server.start()
  }
}