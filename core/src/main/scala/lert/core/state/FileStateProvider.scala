package lert.core.state

import java.nio.file.{Files, Path}
import javax.inject.Inject

import com.fasterxml.jackson.databind.ObjectMapper
import com.typesafe.scalalogging.LazyLogging
import lert.core.config.ConfigProvider

class FileStateProvider @Inject()(objectMapper: ObjectMapper, configProvider: ConfigProvider) extends StateProvider with LazyLogging {

  override def logRule(status: State): Unit =
    Files.write(ruleNameToPath(status.ruleName), objectMapper.writeValueAsString(status).getBytes)

  override def getRuleStatus(ruleName: String): Option[State] = {
    try {
      Some(objectMapper.readValue(Files.readAllBytes(ruleNameToPath(ruleName)), classOf[RuleState]))
    } catch {
      case ex: Exception =>
        logger.warn(s"Couldn't read previous rule's [$ruleName] status: ${ex.getLocalizedMessage}")
        None
    }
  }

  private def ruleNameToPath(ruleName: String): Path = configProvider.getLertHome().resolve(ruleName)
}