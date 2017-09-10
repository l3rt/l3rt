package lert.core.config

import lert.core.BaseSpec

class ArgumentProviderSpec extends BaseSpec {
  it should "read arguments" in {
    val arguments = new ArgumentProvider(Array("--config", "/config", "--rules", "/rules")).arguments
    assert(arguments.config == "/config")
    assert(arguments.rules == "/rules")
  }
}
