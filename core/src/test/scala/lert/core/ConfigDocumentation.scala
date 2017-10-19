package lert.core

import java.io.File
import java.lang.reflect.{Field, ParameterizedType}
import java.nio.file.{Files, Paths}

import lert.core.config.{Config, Description}

import scala.util.Try

class ConfigDocumentation extends BaseSpec {
  it should "generate config docs" in {
    if (System.getenv("SKIP_DOC_GENERATION") == null) {
      ConfigDocumentation.main(Array())
    }
  }
}

object ConfigDocumentation extends App {
  val sb = new StringBuilder
  generateForClass(classOf[Config], sb, 0)

  Seq("./docs/config.md", "../docs/config.md")
    .map(Paths.get(_))
    .find(Files.exists(_))
    .foreach { p =>
      Files.write(p,
        s"""
           |# Configuration
           |
      |${sb.toString()}
    """.stripMargin.getBytes)
      println("Done!!!")
    }

  def generateForClass(cls: Class[_], sb: StringBuilder, level: Int): Unit = {
    cls.getDeclaredFields.foreach { f =>
      val description = f.getAnnotationsByType(classOf[Description]).headOption.map(_.value()).getOrElse("")
      val indent = (0 until level).map(_ => "\t").mkString("")
      sb.append(s"$indent* `${f.getName}` (${f.getType.getSimpleName}) - $description \n")

      val genericClass = getGenericClass(f)
      val nestedClass = if (genericClass != null) genericClass else f.getType

      if (nestedClass.getPackage == classOf[Config].getPackage) {
        generateForClass(nestedClass, sb, level + 1)
      }
    }
  }

  def getGenericClass(f: Field): Class[_] = {
    val isSeq = classOf[Seq[_]].isAssignableFrom(f.getType)
    if (isSeq) {
      Try(f.getGenericType.asInstanceOf[ParameterizedType].getActualTypeArguments()(0).asInstanceOf[Class[_]])
        .getOrElse(null)
    } else {
      null
    }
  }
}
