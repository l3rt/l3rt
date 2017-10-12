package lert.core.config

import java.io.InputStream
import java.nio.file.{Files, Path, Paths}
import java.util
import javax.inject.Inject

import com.fasterxml.jackson.databind.ObjectMapper
import com.typesafe.config.{ConfigFactory, Config => TypesafeConfig}
import lert.core.config.ConfigProvider._
import pureconfig._

trait ConfigParser {
  def read(is: InputStream): Config
}

class JsonConfigParser @Inject()(objectMapper: ObjectMapper) extends ConfigParser {
  def read(is: InputStream): Config = {
    objectMapper.readValue(is, classOf[Config])
  }
}

trait ConfigProvider {
  def config(implicit configOverrider: ConfigOverrider = null): Config

  def getLertHome(): Path = {
    val lertHome = Option(config.home).map(Paths.get(_)).getOrElse(Paths.get(HOME_DIR, LERT_TEMP_DIR))
    if (!Files.exists(lertHome)) {
      Files.createDirectories(lertHome)
    }
    lertHome
  }
}

object ConfigProvider {
  private val HOME_DIR = System.getProperty("user.home")
  private val LERT_TEMP_DIR = ".l3rt"
}

class PureConfigProvider extends ConfigProvider {
  private implicit def hint[T] = ProductHint[T](ConfigFieldMapping(CamelCase, CamelCase))

  lazy val baseConfig: TypesafeConfig = loadConfigOrThrow[TypesafeConfig]

  override def config(implicit configOverrider: ConfigOverrider): Config =
    loadConfigWithFallbackOrThrow[Config](
      if (configOverrider == null) baseConfig else ConfigFactory.parseMap(configOverrider.map).withFallback(baseConfig)
    )

}

case class ConfigOverrider(map: util.Map[String, _])