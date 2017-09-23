package lert.rest

import com.google.inject.{Inject, Injector, Singleton}
import com.typesafe.scalalogging.LazyLogging
import io.undertow.Undertow
import lert.core.utils.ClosableModule
import net.codingwell.scalaguice.ScalaModule

class RestModule extends ScalaModule with ClosableModule {

  override def configure(): Unit = {
    requestInjection(this)
  }

  @Inject def initTaskManager(instance: Server): Unit = {
    instance.start()
  }

  override def close(injector: Injector): Unit = {
    injector.getInstance(classOf[Server]).stop()
  }
}

@Singleton
class Server @Inject()(controller: Controller) extends LazyLogging {
  var server: Undertow = _

  def start(): Unit = {
    logger.info("HTTP server is being started")

    server = Undertow.builder
      .addHttpListener(8080, "0.0.0.0", controller)
      .build

    server.start()
  }

  def stop(): Unit = server.stop()
}