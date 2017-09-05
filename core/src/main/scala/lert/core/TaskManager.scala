package lert.core

import java.util.concurrent.{Executors, TimeUnit}

import scala.util.{Failure, Try}

import lert.core.config.ConfigProvider
import lert.core.rule.RuleLoader
import com.google.inject.Inject
import com.typesafe.scalalogging.LazyLogging

class TaskManager(period: Long, task: Task) extends LazyLogging {
  private val scheduler = Executors.newSingleThreadScheduledExecutor()

  def start(): Unit = {
    scheduler.scheduleWithFixedDelay(task, 0, period, TimeUnit.MILLISECONDS)
    logger.info("Task manager has been started")
  }

  def stop(): Unit = scheduler.shutdownNow()
}

class Task @Inject()(configProvider: ConfigProvider,
                     ruleLoader: RuleLoader) extends Runnable with LazyLogging {
  override def run(): Unit = {
    try {
      val config = configProvider.config
      require(config != null, "Config is not specified")

      logger.debug("Task is being run")
      config
        .rules
        .foreach(ruleLoader.process)
    } catch {
      case ex: Any =>
        logger.error(ex.getLocalizedMessage)
        logger.debug(ex.getLocalizedMessage, ex)
    }
  }

}
