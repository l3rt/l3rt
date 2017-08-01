package lert.core.utils

import java.util.Properties

import scala.collection.JavaConverters._

object JavaUtils {
  /**
    * Converts all Scala collections to Java ones. Non-collection arguments and
    * elements of input collections are returned as is
    */
  def toJava(x: Any): Any = {
    x match {
      case y: scala.collection.MapLike[_, _, _] =>
        y.map({ case (d, v) => toJava(d) -> toJava(v) }).asJava
      case y: scala.collection.SetLike[_, _] =>
        y.map({ item: Any => toJava(item) }).asJava
      case y: Iterable[_] =>
        y.map({ item: Any => toJava(item) }).asJava
      case y: Iterator[_] =>
        toJava(y.toIterable)
      case _ =>
        x
    }
  }

  def toProperties(properties: Map[String, String]): Properties =
    (new Properties /: properties) {
      case (a, (k, v)) =>
        a.put(k, v)
        a
    }

}
