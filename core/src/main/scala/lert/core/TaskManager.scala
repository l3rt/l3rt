package lert.core

import java.util.concurrent.{Executors, TimeUnit}

import com.google.inject.{Inject, Singleton}
import com.typesafe.scalalogging.LazyLogging
import lert.core.config.ConfigProvider
import lert.core.rule.RuleLoader

@Singleton
class TaskManager @Inject()(configProvider: ConfigProvider, task: Task) extends LazyLogging {
  private val scheduler = Executors.newSingleThreadScheduledExecutor()

  def start(): Unit = {
    scheduler.scheduleWithFixedDelay(task, 0, configProvider.config.delay, TimeUnit.MILLISECONDS)
    logger.info("Task manager has been started")
  }

  def stop(): Unit = {
    scheduler.shutdownNow()
    logger.info("Task manager has been stopped")
  }
}

class Task @Inject()(configProvider: ConfigProvider,
                     ruleLoader: RuleLoader) extends Runnable with LazyLogging {
  override def run(): Unit = {
    try {
      val rules = configProvider.config.rules
      require(rules != null, "Rule location not specified.")

      logger.debug("Task is being run")
      rules
        .split(";")
        .foreach(ruleLoader.process)
    } catch {
      case ex: Any =>
        logger.error(ex.getLocalizedMessage)
        logger.debug(ex.getLocalizedMessage, ex)
    }
  }

}
