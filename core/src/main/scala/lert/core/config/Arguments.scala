package lert.core.config

import javax.inject.{Inject, Named}

case class Arguments(config: String = null, rules: String = null)

class ArgumentProvider @Inject()(@Named("args") args: Array[String]) {
  private val argMap = args.sliding(2, 2).toList.map { case Array(key, value) =>
    key.replaceAll("[-]+(.*)", "$1") -> value
  }.toMap
  private val initial = Arguments()
  classOf[Arguments].getDeclaredFields.toSeq
    .filter { f => argMap.contains(f.getName) }
    .foreach { f =>
      f.setAccessible(true)
      f.set(initial, argMap.getOrElse(f.getName, null))
    }

  def arguments: Arguments = initial
}
