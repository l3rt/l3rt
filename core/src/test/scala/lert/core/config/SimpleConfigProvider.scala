package lert.core.config

import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.typesafe.config.{ConfigFactory, Config => TypesafeConfig}

class SimpleConfigProvider(val conf: Config) extends PureConfigProvider {
  val objectMapper = new ObjectMapper() {
    registerModule(DefaultScalaModule)
    setSerializationInclusion(Include.NON_NULL)
  }
  override lazy val baseConfig: TypesafeConfig =
    ConfigFactory.parseString(objectMapper.writeValueAsString(conf))
}

object SimpleConfigProvider {
  def apply(config: Config) = new SimpleConfigProvider(config)
}
