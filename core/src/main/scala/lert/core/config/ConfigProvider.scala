package lert.core.config

import java.io.InputStream
import java.nio.file.{Files, Paths}
import javax.inject.{Inject, Named}

import com.fasterxml.jackson.databind.ObjectMapper

trait ConfigParser {
  def read(is: InputStream): Config
}

class JsonConfigParser @Inject()(objectMapper: ObjectMapper) extends ConfigParser {
  def read(is: InputStream): Config = {
    objectMapper.readValue(is, classOf[Config])
  }
}

trait ConfigProvider {
  def config: Config
}

class SimpleConfigProvider(val config: Config) extends ConfigProvider

object SimpleConfigProvider {
  def apply(config: Config) = new SimpleConfigProvider(config)
}

class FileConfigProvider @Inject()(configReader: ConfigParser,
                                   @Named("configFile") val configFile: String) extends ConfigProvider {
  override def config: Config = {
    val inputStream = Files.newInputStream(Paths.get(configFile))
    try {
      configReader.read(inputStream)
    } finally {
      inputStream.close()
    }
  }
}