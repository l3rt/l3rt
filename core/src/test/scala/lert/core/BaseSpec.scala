package lert.core

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import org.junit.runner.RunWith
import org.scalatest.FlatSpec
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class BaseSpec extends FlatSpec {
  val objectMapper = new ObjectMapper() {
    registerModule(DefaultScalaModule)
  }
}
