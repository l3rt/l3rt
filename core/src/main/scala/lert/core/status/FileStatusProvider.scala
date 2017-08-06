package lert.core.status

import java.nio.file.{Files, Path, Paths}
import javax.inject.Inject

import com.fasterxml.jackson.databind.ObjectMapper
import com.typesafe.scalalogging.LazyLogging

class FileStatusProvider @Inject()(objectMapper: ObjectMapper) extends StatusProvider with LazyLogging {
  private val TEMP_DIR = System.getProperty("java.io.tmpdir")
  private val LERT_TEMP_DIR = ".lert"
  Files.createDirectories(Paths.get(TEMP_DIR, LERT_TEMP_DIR))

  override def logRule(status: Status): Unit =
    Files.write(ruleNameToPath(status.ruleName), objectMapper.writeValueAsString(status).getBytes)

  override def getRuleStatus(ruleName: String): Option[Status] = {
    try {
      Some(objectMapper.readValue(Files.readAllBytes(ruleNameToPath(ruleName)), classOf[Status]))
    } catch {
      case ex: Exception =>
        logger.warn(s"Couldn't read previous rule's [$ruleName] status: ${ex.getLocalizedMessage}")
        None
    }
  }

  private def ruleNameToPath(ruleName: String): Path =
    Paths.get(TEMP_DIR, LERT_TEMP_DIR, ruleName)

}
