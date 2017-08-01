package lert.core.cache

import lert.core.cache.GuavaCache.Key
import com.google.common.cache.CacheBuilder

trait GlobalCache {
  def get[K, V](resourceType: String, key: K, value: => V): V
}

class GuavaCache extends GlobalCache {

  private val cache = CacheBuilder.newBuilder.build[Object, Object]()

  override def get[K, V](resourceType: String, key: K, value: => V): V =
    cache.get(Key(resourceType, key), () => value.asInstanceOf[Object]).asInstanceOf[V]
}

object GuavaCache {

  case class Key[V](resourceType: String, key: V)

}