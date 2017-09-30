package lert.core.config

import java.util
import java.util.Collections

import lert.core.BaseSpec

import scala.collection.JavaConverters._

class ConfigProviderSpec extends BaseSpec {
  it should "read a json config with all nested params" in {
    System.setProperty("config.file", this.getClass.getClassLoader.getResource("example-config.json").getFile)
    val provider = new PureConfigProvider()
    val config = provider.config

    assert(config.rules == "/lert/rules/")
    assert(config.sources.head.url == "elasticSearch:http://localhost:9200")
    assert(config.targetSettings.hipchat.accessToken == "test")
    assert(config.targetSettings.mailServer == null)
  }

  it should "override config" in {
    System.setProperty("config.file", this.getClass.getClassLoader.getResource("example-config.json").getFile)
    val provider = new PureConfigProvider()
    var config: Config = null
    var newConfig: Config = null

    {
      config = provider.config
    }

    {
      implicit val configOverrider: ConfigOverrider = ConfigOverrider(
        Map(
          "rules" -> "/newlocation",
          "sources" -> util.Arrays.asList(
            Map(
              "url" -> "newValue"
            ).asJava
          )
        ).asJava
      )
      newConfig = provider.config
    }

    assert(config.rules == "/lert/rules/")
    assert(config.sources.head.url == "elasticSearch:http://localhost:9200")
    assert(newConfig.rules == "/newlocation")
    assert(newConfig.sources.head.url == "newValue")
  }
}
