package io.github.chenfh5

import java.util
import java.util.Collections.synchronizedMap

class LRUCache[K, V](maxEntries: Int) extends java.util.LinkedHashMap[K, V](100, .75f, true) {
  override def removeEldestEntry(eldest: java.util.Map.Entry[K, V]): Boolean = size > maxEntries
}

object LRUCache {
  def apply[K, V](maxEntries: Int): util.Map[K, V] = synchronizedMap(new LRUCache[K, V](maxEntries))
}
